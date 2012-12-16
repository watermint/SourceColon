$:.unshift File.expand_path('../lib', File.dirname(__FILE__))

require 'rubygems'
require 'sourcecolon/api'

run SourceColon::Api
