#!/bin/bash

# Purpose: Script is used to switch to the failover archive if the primary is unhealthy, by altering the macros
# file and then reloading Httpd.
# Crontab for every minute: * * * * * /path/to/switchoverArchive.sh
help() {
    echo "$0 PATH_TO_HTTPD_MACROS_FILE TIMEOUT_FIRST_CURL TIMEOUT_SECOND_CURL"
    echo ""
    echo "Script used to automatically update the archive location (to the failover) in httpd if the primary is down."
    echo "Pass in the path to the macros file containing the archive definitions; the timeout of the first curl check; and the timeout of the second curl check."
    exit 2
}

if [ $# -eq 0 ]; then
    help
fi

MACROS_PATH=$1
# Connection timeouts for curl requests (the time waited for a connection to be established). The second should be longer
# as we want to be confident the main archive is in fact "down" before switching.
TIMEOUT1=$2
TIMEOUT2=$3
# These next lines get the current ip values for the archive and failover, plus they store the value of production,
# which is a variable pointing to either the primary or failover value.
archiveIp="$(sed -n -e 's/^Define ARCHIVE_IP \(.*\)/\1/p' ${MACROS_PATH} | tr -d '[:space:]')"
failoverIp="$(sed -n -e 's/^Define ARCHIVE_FAILOVER_IP \(.*\)/\1/p' ${MACROS_PATH} | tr -d '[:space:]')"
productionIp="$(sed -n -e 's/^Define PRODUCTION_ARCHIVE \(.*\)/\1/p' ${MACROS_PATH} | tr -d '[:space:]')"
# Checks if the macro.conf is set as healthy or unhealthy currently.
if [[ "${productionIp}" == "\${ARCHIVE_IP}" ]]
then
    alreadyHealthy=1
    logger -t archive "currently healthy"
else
    alreadyHealthy=0
    logger -t archive "currently unhealthy"
fi
# Sets the production value to point to the variable defining the main archive IP, provided it isn't already set.
setProductionMainIfNotSet() {
    if [[ $alreadyHealthy -eq 0 ]]
    then
        # currently unhealthy
        # set production to archive
        logger -t archive "Healthy: setting production to main archive"
        sed -i -e   "s/^Define PRODUCTION_ARCHIVE .*/Define PRODUCTION_ARCHIVE \${ARCHIVE_IP}/"  ${MACROS_PATH}
        systemctl reload httpd
        notify-operators "Healthy: main archive online"
    else
        # If already healthy then no reload or notification occurs.
        logger -t archive "Healthy: already set, no change needed"
    fi
}


setFailoverIfNotSet() {
    if [[ $alreadyHealthy -eq 1 ]]
    then
        # Set production to failover if not already. Separate if statement in case the curl statement
        # fails but the production is already set to point to the backup
        sed -i -e  "s/^Define PRODUCTION_ARCHIVE .*/Define PRODUCTION_ARCHIVE \${ARCHIVE_FAILOVER_IP}/"  ${MACROS_PATH}
        logger -t archive "Unhealthy: second check failed, switching to failover"
        systemctl reload httpd
        notify-operators "Unhealthy: main archive offline"
    else
        logger -t archive "Unhealthy: second check still fails, failover already in use"
    fi
}

logger -t archive "begin check"
curl -s --connect-timeout ${TIMEOUT1} "http://${archiveIp}:8888/gwt/status" >> /dev/null
if [[ $? -ne 0 ]]
then
    logger -t archive "first check failed"
    curl -s --connect-timeout ${TIMEOUT2} "http://${archiveIp}:8888/gwt/status" >> /dev/null
    if [[ $? -ne 0 ]]
    then
        setFailoverIfNotSet
    else
        setProductionMainIfNotSet
    fi
else
    setProductionMainIfNotSet
fi
