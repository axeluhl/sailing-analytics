#!/bin/sh
# starts the application in foreground and debug mode

export CHAMELEON_DEBUG=false
export CHAMELEON_RELOAD=true

cd ${buildout:directory}
bin/paster serve --reload ${buildout:directory}/etc/webserver-development.ini
