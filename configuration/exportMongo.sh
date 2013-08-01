#!/bin/bash
MONGOPORTS=$@
echo "Exporting data from $MONOGPORTS..."

for PORT in $MONGOPORTS
do
    MONGOEXPORT="/opt/mongodb/bin/mongoexport --port $PORT -d winddb"
    echo $MONGOEXPORT
    $MONGOEXPORT -c EVENTS > EVENTS-$PORT.json
    $MONGOEXPORT -c LEADERBOARD_GROUPS > LEADERBOARD_GROUPS-$PORT.json
    $MONGOEXPORT -c LEADERBOARDS > LEADERBOARDS-$PORT.json
    $MONGOEXPORT -c RACE_LOGS > RACE_LOGS-$PORT.json
    $MONGOEXPORT -c REGATTA_FOR_RACE_ID > REGATTA_FOR_RACE_ID-$PORT.json
    $MONGOEXPORT -c REGATTAS > REGATTAS-$PORT.json
    $MONGOEXPORT -c TRACTRAC_CONFIGURATIONS > TRACTRAC_CONFIGURATIONS-$PORT.json
    echo "Exporting wind data... (can take a loooong time)"
    $MONGOEXPORT -c WIND_TRACKS > WIND_TRACKS-$PORT.json
done

echo "Compressing..."
tar cvzf exported.tar.gz *.json
rm *.json
echo "OK"
