#!/bin/bash
# Defines reverse proxy mappings in /etc/httpd/conf.d based on the $SERVER_NAME property.
# A .conf file named after the to-lowercase version of $SERVER_NAME will be created under
# /etc/httpd/conf.d, and based on the settings of EVENT_ID, EVENT_HOSTNAME, SERIES_ID,
# and SERIES_HOSTNAME, one or more Event-SSL, Series-SSL, or a default Home-SSL entry will
# be made in the .conf file.
# NOTE: The httpd server will *not* be reloaded by this script!

source env.sh
APACHE_CONFIG_DIR=/etc/httpd/conf.d

server_name="`echo ${SERVER_NAME} | tr [A-Z] [a-z]`"
if [ ${#EVENT_HOSTNAME[*]} = 0 ]; then
  echo "No dedicated hostname provided for reverse proxy mapping for event. Defaulting to server name ${server_name}" >>/var/log/sailing.err
  EVENT_HOSTNAME="${server_name}.sapsailing.com"
fi
if [ ${#SERIES_HOSTNAME[*]} = 0 ]; then
  echo "No dedicated hostname provided for reverse proxy mapping for series. Defaulting to server name ${server_name}" >>/var/log/sailing.err
  SERIES_HOSTNAME="${server_name}.sapsailing.com"
fi
reverse_proxy_config_file="$APACHE_CONFIG_DIR/${server_name}.conf"
if [ $(( ${#EVENT_ID[*]} + ${#SERIES_ID[*]} )) = 0 ]; then
  # neither an EVENT_ID nor a SERIES_ID were provided; default to Home-SSL macro invocation:
  echo "## No event / series ID provided; forwarding domain to /gwt/Home.html" >>"${reverse_proxy_config_file}"
  echo "Use Home-SSL ${EVENT_HOSTNAME} 127.0.0.1 $SERVER_PORT" >>"${reverse_proxy_config_file}"
else
  # Now loop over all EVENT_ID specification keys and produce an "Event-SSL" macro invocation for each:
  for i in ${!EVENT_ID[*]}; do
    echo "Appending macro invocation to ${reverse_proxy_config_file} to map ${EVENT_HOSTNAME[$i]} to event with ID ${EVENT_ID[$i]} with server running on local port $SERVER_PORT..." >>/var/log/sailing.err
    if [ -z "${EVENT_HOSTNAME[$i]}" ]; then
      EVENT_HOSTNAME[$i]="${SERVER_NAME}.sapsailing.com"
    fi
    echo "## EVENT ${EVENT_HOSTNAME[$i]}" >>"${reverse_proxy_config_file}"
    echo "Use Event-SSL ${EVENT_HOSTNAME[$i]} \"${EVENT_ID[$i]}\" 127.0.0.1 $SERVER_PORT" >>"${reverse_proxy_config_file}"
  done
  # Now loop over all SERIES_ID specification keys and produce an "Series-SSL" macro invocation for each:
  for i in ${!SERIES_ID[*]}; do
    if [ -z "${SERIES_HOSTNAME[$i]}" ]; then
      SERIES_HOSTNAME[$i]="${SERVER_NAME}.sapsailing.com"
    fi
    echo "Appending macro invocation to ${reverse_proxy_config_file} to map ${SERIES_HOSTNAME[$i]} to event with ID ${SERIES_ID[$i]} with server running on local port $SERVER_PORT..." >>/var/log/sailing.err
    echo "## SERIES ${SERIES_HOSTNAME[$i]}" >>"${reverse_proxy_config_file}"
    echo "Use Series-SSL ${SERIES_HOSTNAME[$i]} \"${SERIES_ID[$i]}\" 127.0.0.1 $SERVER_PORT" >>"${reverse_proxy_config_file}"
  done
fi
