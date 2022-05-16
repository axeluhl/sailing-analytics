#!/bin/bash
./updateEclipseLaunchers.sh ../raceanalysis.product ../../com.sap.sailing.server/*.launch
./updateSeleniumPom.sh ../raceanalysis.product ../../com.sap.sailing.selenium.test/pom.xml
echo "Done. Don't forget to refresh your Eclipse workspace."
