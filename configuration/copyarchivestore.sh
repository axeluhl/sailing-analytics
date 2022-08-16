#!/bin/sh
# Start by exporting the production archive SecurityService-related data 
./export_security.sh archive winddb 10201
# Then import into a new security_service DB on the live replica set
./import_security.sh archive security_service 10203
