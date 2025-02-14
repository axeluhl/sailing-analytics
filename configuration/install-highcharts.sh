#!/bin/bash
if [ -z "${TMP}" ]; then
    TMP=/tmp
fi
HIGHCHARTS="$( mktemp -d ${TMP}/highchartsXXXXX )"
TARGET="$( realpath $( dirname ${0} ))/../java/com.sap.sse.gwt/src/com/sap/sse/gwt/resources/highcharts"
echo "Will copy git files to ${TARGET}"
echo "Cloning highcharts-dist to $HIGHCHARTS ..."
cd "${HIGHCHARTS}"
git clone git@github.com:highcharts/highcharts-dist.git
cd "${TARGET}"
echo "Now in directory $(pwd)"
for i in $( find . -type f ); do
    cp "${HIGHCHARTS}/highcharts-dist/${i}" "${i}"
done
rm -rf "${HIGHCHARTS}"
