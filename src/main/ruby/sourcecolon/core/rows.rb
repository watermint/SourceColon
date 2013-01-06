module SourceColon
  class Core::Rows
    def list(path_hash, row_start, row_end)
      list = []

      begin
        q = org.apache.solr.client.solrj.SolrQuery.new
        q.set_rows(row_end - row_start)
        q.set_query("path_hash:#{path_hash} AND text_seq:[#{row_start} TO #{row_end}]")

        r = solr_server.query(q)
        r.get_results.each do |d|
          seq = d.get_first_value("text_seq")
          txt = d.get_first_value("text")

          next if seq.nil? || txt.nil?

          list << {seq.to_i => txt}
        end
      rescue Exception => e
        p e.backtrace
      end

      list.to_json
    end

    def solr_server
      org.watermint.sourcecolon.Config.get_instance.get_server
    end
  end
end