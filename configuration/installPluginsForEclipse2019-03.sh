#!/bin/bash

# You need an installation of the Photon release of "Eclipse IDE for Eclipse Committers" matching you OS and JDK (32 vs 64 Bit):
# https://www.eclipse.org/downloads/packages/release/2019-03/r/eclipse-ide-eclipse-committers

if [[ $1 == "" ]]; then
    echo "You need to specify the Eclipse installation directory"
    exit 1
fi

installPath="$1"

installPlugins() {
    "$installPath/eclipse" -application org.eclipse.equinox.p2.director -noSplash -roaming -repository $1 -installIU $2
	echo ""
}

updatePlugins() {
    "$installPath/eclipse" -application org.eclipse.equinox.p2.director -noSplash -roaming -repository $1 -uninstallIU $2 -installIU $2
	echo ""
}

# Not necessary if using Eclipse for JEE developers
echo "Installing webtools (WTP/WST/JPT) and m2e that are required by the GWT plugin..."
installPlugins http://download.eclipse.org/releases/2019-03/ org.eclipse.wst.web_ui.feature.feature.group,org.eclipse.jst.web_ui.feature.feature.group,org.eclipse.wst.xml_ui.feature.feature.group,org.eclipse.jpt.common.feature.feature.group,org.eclipse.jpt.jpa.feature.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.m2e.wtp.feature.feature.group

echo "Installing GWT plugin..."
installPlugins http://storage.googleapis.com/gwt-eclipse-plugin/v3/release com.gwtplugins.eclipse.suite.v3.feature.feature.group

echo "Installing Mission Control plugin..."
installPlugins http://download.oracle.com/technology/products/missioncontrol/updatesites/base/6.0.0/eclipse com.oracle.jmc.feature.ide.feature.group,com.oracle.jmc.feature.flightrecorder.feature.group,com.oracle.jmc.feature.core.feature.group,com.oracle.jmc.feature.console.feature.group,com.oracle.jmc.feature.core.feature.group,com.oracle.jmc.feature.core.feature.group

echo "Installing GWT SDM debug bridge..."
installPlugins http://p2.sapsailing.com/p2/sdbg com.github.sdbg.feature.feature.group

echo "Installing EasyShell..."
installPlugins http://anb0s.github.io/EasyShell de.anbos.eclipse.easyshell.feature.feature.group

echo "Installing BIRT charts (requirement for MAT)..."
installPlugins https://download.eclipse.org/birt/update-site/photon-interim/ org.eclipse.birt.chart.feature.group

echo "Installing Memory Analyzer..."
installPlugins http://download.eclipse.org/releases/2019-03 org.eclipse.mat.feature.feature.group,org.eclipse.mat.chart.feature.feature.group

echo "Installing SAP JVM Tools (profiler) ..."
installPlugins https://tools.hana.ondemand.com/oxygen com.sap.jvm.profiling.feature.group

echo "Installing UMLet ..."
installPlugins https://www.umlet.com/umlet_latest/repository/ umlet-eclipse-feature.feature.group

echo "Installing javax.xml.bind for news feed polling (see https://stackoverflow.com/questions/52528693/eclipse-internal-error-polling-news-feeds)..."
installPlugins http://download.eclipse.org/tools/orbit/downloads/drops/R20180905201904/repository javax.xml.bind

echo "Installation completed!"
