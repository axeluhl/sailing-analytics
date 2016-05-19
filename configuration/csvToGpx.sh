#!/bin/sh
CSV=$1
GPX=$2
if [ "$GPX" = "" ]; then
  GPX="${CSV}.gpx"
fi

# TODO extract trackname from a CSV column
TRACKNAME=`head -n 2 "$CSV" | tail -n 1 | awk --field-separator , '{ print $1; }'`

cat << "EOF" >"$GPX"
<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" version="1.1" creator="me">
  <trk>
EOF
echo "    <name>$TRACKNAME</name>
    <trkseg>" >>$GPX
cat "$CSV" | tail -n +2 | \
  awk --field-separator , '{
    split($2, dateWithColons, ":")
    hoursOfDay=int($3/3600)
    minutesOfHour=int(($3-hoursOfDay*3600)/60)
    secondsOfMinute=$3-hoursOfDay*3600-minutesOfHour*60
    printf "      <trkpt lat=\"%f\" lon=\"%f\"><time>%sT%02d:%02d:%06.3fZ</time><course>%0.1f</course><speed>%0.2f</speed></trkpt>\n", $6, $7, dateWithColons[3] "-" dateWithColons[2] "-" dateWithColons[1], hoursOfDay, minutesOfHour, secondsOfMinute, $11, $12/3600*1852;
  }' >>"$GPX"
cat << "EOF" >>"$GPX"
    </trkseg>
  </trk>
</gpx>
EOF
