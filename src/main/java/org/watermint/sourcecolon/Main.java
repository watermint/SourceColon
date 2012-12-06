package org.watermint.sourcecolon;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class Main {
    public static void main(String... args) throws Exception {
        File home = new File("./src/main/resources");
        File configFile = new File(home, "solr.xml");
        CoreContainer container = new CoreContainer(home.getAbsolutePath(), configFile);
        EmbeddedSolrServer server = new EmbeddedSolrServer(container, "sourcecolon");
        try {
            List<SolrInputDocument> docs = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                SolrInputDocument d = new SolrInputDocument();
                d.setField("id", "data" + i);
                d.setField("name", "name" + i);
                docs.add(d);
            }
            server.add(docs);

            server.commit();

            SolrQuery q = new SolrQuery();
            q.setQuery("name:name3*");

            QueryResponse r = server.query(q);

            SolrDocumentList results = r.getResults();
            results.setStart(10);

            for (SolrDocument d : results) {
                System.out.println(d.toString());
            }
        } finally {
            server.shutdown();
        }
    }
}
