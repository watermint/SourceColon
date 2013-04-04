package org.watermint.sourcecolon.solr;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * Configuration.
 * This class is not thread-safe.
 */
public class Config {
    private static Config instance;
    private String homePath;
    private EmbeddedSolrServer server;
    private CoreContainer coreContainer;

    /**
     * Singleton instance.
     *
     * @return config.
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /**
     * Constructor for singleton.
     */
    private Config() {
        this.homePath = System.getProperty("user.home") + File.separator + ".sourcecolon";
        prepare();
    }

    /**
     * Constructor with homePath for unit test.
     *
     * @param homePath home path.
     */
    protected Config(String homePath) {
        this.homePath = homePath;
        prepare();
    }

    /**
     * Home path.
     *
     * @return home path.
     */
    public String getHomePath() {
        return homePath;
    }

    /**
     * Prepare configuration.
     */
    public void prepare() {
        copyIfNecessary("/", "solr.xml");
        copyIfNecessary("/rows/conf", "rows/conf/schema.xml");
        copyIfNecessary("/rows/conf", "rows/conf/solrconfig.xml");
    }

    /**
     * Solr Server.
     *
     * @return server instance.
     */
    public EmbeddedSolrServer getServer() {
        if (coreContainer == null) {
            try {
                coreContainer = new CoreContainer(homePath, new File(homePath + File.separator + "solr.xml"));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new ConfigRuntimeException(e);
            }
        }

        if (server == null) {
            server = new EmbeddedSolrServer(coreContainer, "sourcecolon");
        }

        return server;
    }

    /**
     * Copy file from resource.
     *
     * @param distPath     destination path.
     * @param resourcePath resource path.
     */
    private void copyIfNecessary(String distPath, String resourcePath) {
        File distDir = new File(homePath + File.separator + distPath);
        if (!distDir.exists()) {
            if (!distDir.mkdirs()) {
                throw new ConfigRuntimeException("Failed to create configuration directory : " + distPath);
            }
        }
        if (!distDir.isDirectory()) {
            throw new ConfigRuntimeException("Configuration path is not a directory : " + distPath);
        }

        InputStream r = Config.class.getResourceAsStream("config/" + resourcePath);
        if (r == null) {
            throw new ConfigRuntimeException("Configuration Resource not found for path : " + resourcePath);
        }

        File resourceFile = new File(resourcePath);
        String filename = resourceFile.getName();

        File distFile = new File(distDir, filename);

        try {
            FileUtils.copyInputStreamToFile(r, distFile);
        } catch (IOException e) {
            throw new ConfigRuntimeException(e);
        }
    }
}
