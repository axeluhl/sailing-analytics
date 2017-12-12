#!/bin/sh
wc -l `find */com.sap.* -type f -name '*.java' \! -ipath '*/generated/*' \! -ipath '*/gen/*' \! -ipath '*/generated-sources/*' \! -ipath '*/.generated/*' \! -name R.java \! -ipath './java/com.sap.sailing.gwt.ui/com.sap.sailing.gwt.*' `
