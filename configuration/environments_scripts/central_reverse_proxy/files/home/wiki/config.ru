#!/usr/bin/env ruby
__DIR__ = File.expand_path(File.dirname(__FILE__))
$: << __DIR__
require 'rubygems'
require 'ruby-prof'
require 'app'
Gollum::Page.send :remove_const, :FORMAT_NAMES if defined? Gollum::Page::FORMAT_NAMES
Gollum::Markup.formats.clear
Gollum::Markup.formats[:markdown] = {
 :name => "MarkDown",
 :regexp => /md|mkdn?|mdown|markdown/
}
gollum_path = '/home/wiki/gitwiki'
wiki_options = {:universal_toc => false, :live_preview => false}
App.set(:gollum_path, gollum_path)
App.set(:default_markup, :markdown) # set your favorite markup language
App.set(:wiki_options, wiki_options)
App.set(:authorized_users, YAML.load_file(File.expand_path('users.yml', __DIR__)))
#use Rack::RubyProf, :path => '/tmp/profile'
run App
