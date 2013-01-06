package org.watermint.sourcecolon;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.servlet.ServletHandler;
import org.glassfish.grizzly.servlet.WebappContext;
import org.jruby.rack.RackFilter;
import org.jruby.rack.RackServlet;
import org.jruby.rack.RackServletContextListener;
import org.watermint.sourcecolon.grizzly.JRubyRackServlet;
import org.watermint.sourcecolon.grizzly.JRubyRackServletHandler;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Main {
    private EmbeddedSolrServer getServer() {
        return Config.getInstance().getServer();
    }

    public String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(text.getBytes("UTF-8"));
            StringBuilder hashString = new StringBuilder();

            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }

            return hashString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendFileContents(File f) {
        System.out.println("Index: " + f.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(f)) {
            InputStreamReader r = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(r);
            int seq = 1;

            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                SolrInputDocument d = new SolrInputDocument();
                d.setField("id", f.getAbsolutePath() + ":" + seq);
                d.setField("path", f.getAbsolutePath());
                d.setField("path_hash", hash(f.getAbsolutePath()));
                d.setField("path_ext", FilenameUtils.getExtension(f.getAbsolutePath()));
                d.addField("text", line);
                d.setField("text_seq", seq);

                try {
                    getServer().add(d);
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }

                seq++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendFile(File f, String ext) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files == null) {
                return;
            }
            for (File f0 : files) {
                appendFile(f0, ext);
            }
            return;
        }

        if (f.getName().endsWith(ext)) {
            appendFileContents(f);
        }
    }

    public void appendDocument(String path, String ext) {
        File root = new File(path);
        appendFile(root, ext);
        try {
            getServer().commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }

    public void query(String query) {
        SolrQuery q = new SolrQuery();
        q.setRows(30);
        q.setQuery(query);
        q.setHighlight(true);
        q.setHighlightSimplePre("<span class=\"keyword\">");
        q.setHighlightSimplePost("</span>");
        q.setIncludeScore(true);
        q.setSortField("text_seq", SolrQuery.ORDER.asc);
        q.setGetFieldStatistics(true);

        try {
            QueryResponse response = getServer().query(q);
            for (SolrDocument d : response.getResults()) {
                Map<String, Collection<Object>> m = d.getFieldValuesMap();
                for (String k : m.keySet()) {
                    for (Object v : m.get(k)) {
                        System.out.printf("[%s] : %s\n", k, v);
                    }
                }
            }
            Map<String, Map<String, List<String>>> h = response.getHighlighting();
            if (h != null) {
                for (String k : h.keySet()) {
                    Map<String, List<String>> m = h.get(k);
                    for (String k2 : m.keySet()) {
                        for (String k3 : m.get(k2)) {
                            System.out.printf("Highlighting [%s][%s] => [%s]\n", k, k2, k3);
                        }
                    }
                }
            }

            System.out.println("");
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        getServer().shutdown();
    }

    public void server() throws IOException {
        Console console = System.console();

        if (console == null) {
            System.err.println("No console available.");
//            System.exit(1);
        }

        // HTTP Server
        HttpServer server = new HttpServer();
        ServerConfiguration sc = server.getServerConfiguration();

        NetworkListener listener = new NetworkListener("grizzly", "localhost", 8080);
        server.addListener(listener);

        // Jersey configuration
        Map<String, Object> jerseyParams = new HashMap<>();
        jerseyParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

        ResourceConfig jerseyConfig = new PackagesResourceConfig("org.watermint.sourcecolon.api");
        jerseyConfig.setPropertiesAndFeatures(jerseyParams);

        // add handler for jersey
        sc.addHttpHandler(ContainerFactory.createContainer(HttpHandler.class, jerseyConfig));

        // Servlet for JRuby-Rack
        WebappContext jrubyContext = new WebappContext("JRuby-Rack");
        jrubyContext.addFilter("Rack-Filter", RackFilter.class);
        jrubyContext.addServlet("Rack-Servlet", RackServlet.class);
        jrubyContext.addListener(RackServletContextListener.class);

        JRubyRackServletHandler jrubyHandler = new JRubyRackServletHandler(jrubyContext);

        sc.addHttpHandler(jrubyHandler, "/");

        server.start();

        while (true) {
            if (console == null) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    server.stop();
                }
            } else {
                String command = console.readLine("command 'quit' to terminate: ");
                if ("quit".equals(command)) {
                    server.stop();
                    return;
                }
            }
        }
    }

    public static void usage() {
        System.out.println("usage: SourceColon.jar index path ext");
        System.out.println("usage: SourceColon.jar query query_string");
        System.out.println("usage: SourceColon.jar server");
    }

    public static void main(String... args) throws Exception {
        Main m = new Main();
        if (args.length < 1) {
            usage();
            return;
        }

        try {
            switch (args[0]) {
                case "index":
                    if (args.length < 3) {
                        usage();
                        return;
                    }
                    m.appendDocument(args[1], args[2]);
                    break;
                case "query":
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    m.query(args[1]);
                    break;
                case "server":
                    m.server();
                    break;
            }
        } finally {
            m.shutdown();
        }
    }
}
