#!/bin/bash
MACROS_PATH=$1
archiveIp="$(sed -n -E 's/^Define ARCHIVE_IP (.*)/\1/p' ${MACROS_PATH} | tr -d '[:space:]')"
failoverIp="$(sed -n -E 's/^Define ARCHIVE_FAILOVER_IP (.*)/\1/p' ${MACROS_PATH} | tr -d '[:space:]')"
productionIp="$(sed -n -E 's/^Define PRODUCTION (.*)/\1/p' ${MACROS_PATH} | tr -d '[:space:]')"
if [[ "${productionIp}" == "\${ARCHIVE_IP}" ]] 
then 
	alreadyHealthy=1
	echo "currently healthy"
else
	alreadyHealthy=0
	echo "currently unhealthy"
fi
setProductionMainIfNotSet() {
	if [[ $alreadyHealthy -eq 0 ]] 
	then
		#currently unhealthy
		#set production to archive
		echo "setting production to main archive"
		sed -i -E   "s/Define PRODUCTION .*/Define PRODUCTION \${ARCHIVE_IP}/"  ${MACROS_PATH}
		systemctl reload httpd 
                {
                        echo "To: thomasstokes@yahoo.co.uk"
                        echo Subject: Healthy 
                        echo 
                        echo Healthy: main archive online
                } | /usr/sbin/sendmail -t
	else
		echo "currently healthy; no change needed"
	fi

}
echo "begin"
curl -s --connect-timeout 2 "http://${archiveIp}:8888/gwt/status" >> /dev/null
if [[ $? -ne 0 ]]
then
	echo "first check failed"
	curl -s --connect-timeout 5 "http://${archiveIp}:8888/gwt/status" >> /dev/null
	if [[ $? -ne 0 ]]
	then
		if [[ $alreadyHealthy -eq 1 ]] 
		then
			#set production to failover if not already. Separate if statement in case the curl statement fails but the production is already set to point to the backup 
			sed -i -E  "s/Define PRODUCTION .*/Define PRODUCTION \${ARCHIVE_FAILOVER_IP}/"  ${MACROS_PATH}
			echo "switching to failover"
			systemctl reload httpd 
                        {
                                echo "To: thomasstokes@yahoo.co.uk"
                                echo Subject: Unhealthy 
                                echo 
                                echo Unhealthy: main archive offline
                        } | /usr/sbin/sendmail -t
		else
			echo "unhealthy, failover already in use"
		fi
	else
		setProductionMainIfNotSet
	fi
else 
	setProductionMainIfNotSet
fi
cat ${MACROS_PATH}
