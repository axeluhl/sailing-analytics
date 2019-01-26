#!/bin/bash

BUG_ID="$1"

if [ $# -eq 0 ]; then
    echo "$0 [-t -r ] <bugid>"
    echo ""
    echo "-t Disable tests"
    echo "-r Build release"
    echo
    echo "Constructs a Hudson job for the given bugid"
    echo "Example: $0 -r 4221"
    echo "Builds a Hudson job for bug branch bug4221, linking to the Bugzilla bug and building a release"
    exit 2
fi

options='tr'
while getopts $options option
do
    case $option in
        t) SKIP_TESTS=1;;
        b) BUILD_RELEASE=1;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done

CONFIGFILE=$(mktemp mylocalconfigXXXX.xml)
RESPONSE_HEADERS=$(mktemp responseheadersXXXX)
HUDSON_BASE_URL=https://hudson.sapsailing.com
BUGZILLA_BASE=https://bugzilla.sapsailing.com/bugzilla
COPY_TEMPLATE_JOB=CopyTemplate
read -p "Username: " USERNAME
read -s -p "Password: " PASSWORD
echo
COPY_TEMPLATE_CONFIG_URL="$HUDSON_BASE_URL/job/$COPY_TEMPLATE_JOB/config.xml"
curl -s -X GET $COPY_TEMPLATE_CONFIG_URL -u "$USERNAME:$PASSWORD" -o "$CONFIGFILE"
sed -i -e 's|<description>..*</description>|<description>This is the build job for \&lt;a href=\&quot;'$BUGZILLA_BASE'/show_bug.cgi?id='$BUG_ID'\&quot;\&gt;Bug '$BUG_ID'\&lt;/a\&gt;</description>|' -e 's|<disabled>true</disabled>|<disabled>false</disabled>|' "$CONFIGFILE"
if [ "$SKIP_TESTS" = "1" ]; then
  sed -i -e 's|<command>\([^<]*\)build *</command>|<command>\1-t build</command>|' "$CONFIGFILE"
fi
if [ "$BUILD_RELEASE" = "1" ]; then
  sed -i -e 's|<command>\([^<]*\)</command>|<command>\1 \&\& PATH=/usr/local/bin:${PATH} configuration/buildAndUpdateProduct.sh -n bug'$BUG_ID' -w trac@sapsailing.com -u release</command>|' "$CONFIGFILE"
fi
sed -i -e '/<branches>$/{
N
N
s|<name>[^<]*</name>|<name>bug'$BUG_ID'</name>|
}' "$CONFIGFILE"
curl -D "$RESPONSE_HEADERS" -s -XPOST "$HUDSON_BASE_URL/createItem?name=bug$BUG_ID" -u "$USERNAME:$PASSWORD" --data-binary "@$CONFIGFILE" -H "Content-Type:text/xml" >/dev/null 2>/dev/null
RESPONSE_CODE=$(cat "$RESPONSE_HEADERS" | head -n 1 | cut -d ' ' -f2 )
if [[ "$RESPONSE_CODE" =~ 2.. ]]; then
  echo "Find your new, enabled Hudson job at $HUDSON_BASE_URL/job/bug$BUG_ID/"
else
  echo "Error. HTTP response code $RESPONSE_CODE. Did the job already exist?"
fi
rm "$CONFIGFILE"
rm "$RESPONSE_HEADERS"
