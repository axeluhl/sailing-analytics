#!/bin/bash
SERVER=www.sapsailing.com
if [ "$1" != "" ]; then
  SERVER=$1
fi
echo "BOATCLASS,MANEUVER_TYPE,SPEED_BEFORE_IN_KNOTS,SPEED_AFTER_IN_KNOTS,DIRECTION_CHANGE_IN_DEGREES,MAX_TURN_RATE_IN_DEGREES_PER_SECOND,AVG_TURN_RATE_IN_DEGREES_PER_SECOND,LOWEST_SPEED_IN_KNOTS"
curl "https://${SERVER}/sailingserver/api/v1/regattas" | jq -r '.[].name' | while read REGATTA; do
  echo "*** Regatta $REGATTA..." >&2
  REGATTA_URL_ENCODED=$(echo $REGATTA | sed -e 's/ /%20/g')
  BOATCLASS=$(curl "https://${SERVER}/sailingserver/api/v1/regattas/${REGATTA_URL_ENCODED}" | jq -r '.boatclass')
  curl "https://www.sapsailing.com/sailingserver/api/v1/regattas/${REGATTA_URL_ENCODED}/races" | jq -r '.races[].name' | while read RACE; do
    echo "RACE: $RACE" >&2
    RACE_URL_ENCODED=$(echo $RACE | sed -e 's/ /%20/g')
    echo Fetching maneuvers of regatta $REGATTA_URL_ENCODED, race $RACE_URL_ENCODED... >&2
    echo "URL: https://www.sapsailing.com/sailingserver/api/v1/regattas/${REGATTA_URL_ENCODED}/races/${RACE_URL_ENCODED}/maneuvers" >&2
    curl "https://www.sapsailing.com/sailingserver/api/v1/regattas/${REGATTA_URL_ENCODED}/races/${RACE_URL_ENCODED}/maneuvers" | jq -r '.bycompetitor[].maneuvers[] | (.maneuverType + "," + (.speedBeforeInKnots | tostring) + "," + (.speedAfterInKnots | tostring) + "," + (.directionChangeInDegrees | tostring) + "," + (.maxTurningRateInDegreesPerSecond | tostring) + "," + (.avgTurningRateInDegreesPerSecond | tostring) + "," + (.lowestSpeedInKnots | tostring))' | sed -e "s/^/${BOATCLASS},/"
  done
done
