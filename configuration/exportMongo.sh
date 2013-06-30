#!/bin/bash
MONGOPORTS=$@
echo "Exporting data from $MONOGPORTS..."

for PORT in $MONGOPORTS
do
    MONGOEXPORT="/opt/mongodb/bin/mongoexport --port $PORT -d winddb"
    echo $MONGOEXPORT
    $MONGOEXPORT -c EVENTS > EVENTS.json
    $MONGOEXPORT -c LEADERBOARD_GROUPS > LEADERBOARD_GROUPS.json
    $MONGOEXPORT -c LEADERBOARDS > LEADERBOARDS.json
    $MONGOEXPORT -c RACE_LOGS > RACE_LOGS.json
    $MONGOEXPORT -c REGATTA_FOR_RACE_ID > REGATTA_FOR_RACE_ID.json
    $MONGOEXPORT -c REGATTAS > REGATTAS.json
    $MONGOEXPORT -c TRACTRAC_CONFIGURATIONS > TRACTRAC_CONFIGURATIONS.json
    $MONGOEXPORT -c WIND_TRACKS > WIND_TRACKS.json
done

echo "Compressing..."
tar cvzf exported.tar.gz *.json
rm *.json
echo "OK"
