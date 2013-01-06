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

    get "/list" do
      content_type :json
      c = org.watermint.sourcecolon.api.Core.new

      begin
        c.get_list("f88b828ffa4b501067d47ca86f7e6fffdc71b830", 1, 10)
      rescue
        ["Error!"]
      end
    end
  end
end
