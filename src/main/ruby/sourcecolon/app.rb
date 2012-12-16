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

    get "/ping" do
      content_type :json
      m = org.watermint.sourcecolon.Mock.new

      ["Pong, ", m.name].to_json
    end
  end
end
