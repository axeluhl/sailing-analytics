#!/bin/bash
time_to_seconds() {
    local h m s
    IFS=: read -r h m s  <<< "$1"
    echo $((10#$h*3600 + 10#$m*60 + 10#$s))
}
#gets the two most recent items in the error log that are health check releated (HCOH)
LAST_TWO=$(tail  /var/log/httpd/error_log | grep HCOH  | tail -n2  | sed -E 's/\[.* (..:..:..).*]*/\1/'  )
#the next few statements split the two times and then convert both to seconds.
FIRST=$(echo $LAST_TWO | sed -E 's/(.*) .*/\1/')
SECOND=$(echo $LAST_TWO | sed -E 's/.* (.*)/\1/' )
FIRST_SECONDS=$(time_to_seconds "$FIRST")
SECOND_SECONDS=$(time_to_seconds "$SECOND")
DIFF=$(($SECOND_SECONDS-$FIRST_SECONDS))
CURRENT_SECONDS=$(time_to_seconds "$(date +"%H:%M:%S")")
TIME_SINCE_LAST=$(($CURRENT_SECONDS - $SECOND_SECONDS))
CRON_MINUTES=5
CURRENT_DAY=$(date | grep -o '^... ... ..' | head -n1)
FIRST_DAY=$(tail  /var/log/httpd/error_log | grep HCOH  | tail -n1 | grep -o '[A-Z].. ... ..' | head -n1)
SECOND_DAY=$(tail  /var/log/httpd/error_log | grep HCOH  | tail -n2 | head -n1 |  grep -o '[A-Z].. ... ..' | head -n1)
#could replace two with if first day equals current, because this will require the second day be the same?
if [[ "$FIRST_DAY" == "$SECOND_DAY" && "$CURRENT_DAY" == "$SECOND_DAY" && "$DIFF" -le 60 && "$TIME_SINCE_LAST" -le $((60*5)) ]]; #It must be 2 consecutive fails and within 5 minutes.
then
    #postfix and set alert var, in case we only want to alert once
    echo "issue" | /usr/sbin/sendmail "thomasstokes@yahoo.co.uk"
   
else
    #currently ok
    echo "fine"
fi
