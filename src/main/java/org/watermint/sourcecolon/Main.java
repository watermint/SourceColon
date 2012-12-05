package org.watermint.sourcecolon;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
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
    public static class SimpleItem {
        @Field
        String id;

        @Field
        String name;
    }

    public static void main(String... args) throws Exception {
        File home = new File("./src/main/resources");
        File configFile = new File(home, "solr.xml");
        CoreContainer container = new CoreContainer(home.getAbsolutePath(), configFile);
        EmbeddedSolrServer server = new EmbeddedSolrServer(container, "sourcecolon");

        SimpleItem i = new SimpleItem();
        i.id = "d1";
        i.name = "hoge";

        server.addBean(i);
        server.commit();

        SolrQuery q = new SolrQuery();
        q.setQuery("*");

        QueryResponse r = server.query(q);

        List<SimpleItem> results = r.getBeans(SimpleItem.class);

        for (SimpleItem si : results) {
            System.out.println("id : " + si.id);
            System.out.println("name : " + si.name);
        }

        server.shutdown();
    }
}
