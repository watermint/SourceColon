require 'sinatra/base'
require 'json'

include Java

module SourceColon
  class Api < Sinatra::Base
    get "/ping" do
      content_type :json
      m = org.watermint.sourcecolon.Mock.new

      ["Pong, ", m.name].to_json
    end
  end
end