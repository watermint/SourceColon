package org.watermint.sourcecolon;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 *
 */
public class Main {
    private EmbeddedSolrServer server = null;

    public EmbeddedSolrServer getServer() {
        synchronized (this) {
            if (server == null) {
                File home = new File("./src/main/resources");
                File configFile = new File(home, "solr.xml");
                CoreContainer container = null;
                try {
                    container = new CoreContainer(home.getAbsolutePath(), configFile);
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    e.printStackTrace();
                }
                server = new EmbeddedSolrServer(container, "sourcecolon");
            }

        }
        return server;
    }

    public void appendFileContents(File f) {
        System.out.println("Index: " + f.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(f)) {
            InputStreamReader r = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(r);
            int lineNumber = 1;

            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                SolrInputDocument d = new SolrInputDocument();
                d.setField("id", f.getAbsolutePath());
                d.setField("name", f.getName());
                d.setField("line_body", line);
                d.setField("line_number", lineNumber);

                try {
                    getServer().add(d);
                } catch (SolrServerException | IOException e) {
                    e.printStackTrace();
                }

                lineNumber++;
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
        q.setQuery(query);
        try {
            QueryResponse response = getServer().query(q);
            for (SolrDocument d : response.getResults()) {
                System.out.println(d.getFirstValue("name") + "[" + d.getFirstValue("line_number") + "]: " + d.getFirstValue("line_body"));
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        getServer().shutdown();
    }

    public static void usage() {
        System.out.println("usage: SourceColon.jar index path ext");
        System.out.println("usage: SourceColon.jar query query_string");
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
            }
        } finally {
            m.shutdown();
        }
    }
}
