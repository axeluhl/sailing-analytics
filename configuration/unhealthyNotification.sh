#!/bin/bash
time_to_seconds() {
    local h m s
    IFS=: read -r h m s  <<< "$1"
    echo $((10#$h*3600 + 10#$m*60 + 10#$s))
}

LAST_TWO=$(tail  /var/log/httpd/error_log | grep HCOH  | tail -n2  | sed -E 's/\[.* (..:..:..).*]*/\1/'  )
FIRST=$(echo $LAST_TWO | sed -E 's/(.*) .*/\1/')
SECOND=$(echo $LAST_TWO | sed -E 's/.* (.*)/\1/' )
FIRST_SECONDS=$(time_to_seconds "$FIRST")
SECOND_SECONDS=$(time_to_seconds "$SECOND")
DIFF=$(($SECOND_SECONDS-$FIRST_SECONDS))
CURRENT_SECONDS=$(time_to_seconds "$(date +"%H:%M:%S")")
echo "$SECOND_SECONDS $CURRENT_SECONDS"
TIME_SINCE_LAST=$(($CURRENT_SECONDS - $SECOND_SECONDS))
echo "$TIME_SINCE_LAST < $((60*5))"
CRON_MINUTES=5
echo "$DIFF"
if [[ "$DIFF" -le "60" && "$TIME_SINCE_LAST" -le "$((60*5))" ]] #It must be 2 consecutive fails and recent.
then
    #postfix
else
    #currently ok
fi


