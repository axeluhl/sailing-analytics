#!/usr/bin/env bash

# ------------------------------------------------------
# The following variables were part of a bash template
# and could be useful in the future
# ------------------------------------------------------

# SCRIPTNAME
scriptName=`basename $0` #Set Script Name variable
scriptBasename="$(basename ${scriptName} .sh)" # Strips '.sh' from scriptName

# TIMESTAMPS

now=$(LC_ALL=C date +"%Y-%m-%d %H:%M:%S")        # Returns: 2015-06-15 22:34:40
datestamp=$(LC_ALL=C date +%Y-%m-%d)       # Returns: 2015-06-14
hourstamp=$(LC_ALL=C date +%r)             # Returns: 10:34:40 PM
timestamp=$(LC_ALL=C date +%Y%m%d_%H%M%S)  # Returns: 20150614_223440
today=$(LC_ALL=C date +"%m-%d-%Y")         # Returns: 06-14-2015
longdate=$(LC_ALL=C date +"%a, %d %b %Y %H:%M:%S %z")  # Returns: Sun, 10 Jan 2016 20:47:53 -0500
gmtdate=$(LC_ALL=C date -u -R | sed 's/\+0000/GMT/') # Returns: Wed, 13 Jan 2016 15:55:29 GMT

# THISHOST
thisHost=$(hostname)
