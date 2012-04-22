#!/bin/sh
# starts the application in daemon (production) mode

export CHAMELEON_DEBUG=false
export CHAMELEON_RELOAD=false

cd ${buildout:directory}
bin/paster serve --daemon ${buildout:directory}/etc/webserver-production.ini

