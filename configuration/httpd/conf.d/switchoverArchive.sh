#!/bin/bash
archiveIp="$(sed -n -E 's/^Define ARCHIVE_IP (.*)/\1/p' /etc/httpd/conf.d/macros.conf | tr -d '[:space:]')"
failoverIp="$(sed -n -E 's/^Define ARCHIVE_FAILOVER_IP (.*)/\1/p' /etc/httpd/conf.d/macros.conf | tr -d '[:space:]')"
productionIp="$(sed -n -E 's/^Define PRODUCTION (.*)/\1/p' /etc/httpd/conf.d/macros.conf | tr -d '[:space:]')"
if [[ "${productionIp}" == "${archiveIp}" ]] 
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
		echo "Healthy: setting production to main archive"
		sed -i -E   "s/Define PRODUCTION .*/Define PRODUCTION ${archiveIp}/"  /etc/httpd/conf.d/macros.conf
		systemctl reload httpd 
	else
		echo "Healthy already; no change needed"
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
                echo "second check failed"
		if [[ $alreadyHealthy -eq 1 ]] 
		then
			#set production to failover if not already. Separate if statement in case the curl statement fails but the production is already set to point to the backup 
			sed -i -E  "s/Define PRODUCTION .*/Define PRODUCTION ${failoverIp}/"  /etc/httpd/conf.d/macros.conf
			echo "Unhealthy: switching to failover"
			systemctl reload httpd 
		else
			echo "Unhealthy already; failover already in use"
		fi
	else
		setProductionMainIfNotSet
	fi

else 
	setProductionMainIfNotSet
fi

cat /etc/httpd/conf.d/macros.conf
