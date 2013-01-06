module SourceColon
  class Core::Query
    def query(query_string)
      q = org.apache.solr.client.solrj.SolrQuery.new
      q.set_query(query_string)
      q.set_highlight(true)
      q.set_highlight_simple_pre('<span class="label label-info">')
      q.set_highlight_simple_post('</span>')



    end
  end
end