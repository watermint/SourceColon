$:.unshift File.expand_path('../lib', File.dirname(__FILE__))

require 'rubygems'
require 'sourcecolon/core'
require 'sourcecolon/app'

run SourceColon::App
