#!/bin/sh
# stops the daemonized application

export CHAMELEON_DEBUG=false
export CHAMELEON_RELOAD=false

cd ${buildout:directory}
bin/paster serve --stop-daemon ${buildout:directory}/etc/webserver-production.ini

