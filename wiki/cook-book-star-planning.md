This page contains recipes for common problems.

### Export data from MongoDB

`/opt/mongodb/bin/mongoexport --port 10202 -d winddb -c WIND_TRACKS -q "{'RACE_NAME': 'Muscat Race12'}" > tmp/ess2013-muscat-wind.json`