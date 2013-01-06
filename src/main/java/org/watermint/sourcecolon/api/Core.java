package org.watermint.sourcecolon.api;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.watermint.sourcecolon.Config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class Core {
    private EmbeddedSolrServer getServer() {
        return Config.getInstance().getServer();
    }

    @GET
    @Path("/ping")
    public List<String> ping() {
        List<String> response = new ArrayList<>();
        response.add("pong");
        return response;
    }

    @GET
    @Path("/list/{hash:[a-z0-9]+}/{start:\\d+}-{end:\\d+}")
    public List<Map<Long,String>> getList(@PathParam("hash") String pathHash, @PathParam("start") long start, @PathParam("end") long end) {
        List<Map<Long,String>> list = new ArrayList<>();

        if (end - start < 0) {
            return list;
        }

        try {
            int countRows = (int)(end - start);

            SolrQuery q = new SolrQuery();
            q.setRows(countRows);
            q.setQuery("path_hash:" + pathHash + " AND text_seq:[" + start + " TO " + end + "]");
            q.setSortField("text_seq", SolrQuery.ORDER.asc);

            QueryResponse response = getServer().query(q);
            for (SolrDocument d : response.getResults()) {
                Long seq = (Long) d.getFirstValue("text_seq");
                String text = (String) d.getFirstValue("text");
                if (seq == null || text == null) {
                    continue;
                }
                Map<Long,String> r = new HashMap<>();
                r.put(seq, text);
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
