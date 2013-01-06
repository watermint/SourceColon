require "sinatra/base"
require "json"

include Java

module SourceColon
  class App < Sinatra::Base
    before do
      @context_path = request.env['java.servlet_request'].send("getContextPath")
      @title = 'Source:'
    end

    get "/" do
      erb :index
    end

    get "/api/rows/:hash/:start-:end" do
      content_type :json
      list = []

      path_hash = params[:hash]
      row_start = params[:start].to_i
      row_end   = params[:end].to_i

      rows = SourceColon::Core::Rows.new

      rows.list(path_hash, row_start, row_end).to_json
    end
  end
end
