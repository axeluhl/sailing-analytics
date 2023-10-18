source "https://rubygems.org"

gem 'highline', '> 0'
gem 'json', '> 0'
gem 'fastlane', '> 0'

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
