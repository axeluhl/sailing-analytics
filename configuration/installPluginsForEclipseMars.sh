#!/bin/bash

# You need an installation of the Mars release of "Eclipse IDE for Eclipse Committers" matching you OS and JDK (32 vs 64 Bit):
# http://www.eclipse.org/downloads/packages/eclipse-ide-eclipse-committers/marsr

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
installPlugins http://download.eclipse.org/releases/mars org.eclipse.wst.web_ui.feature.feature.group,org.eclipse.jst.web_ui.feature.feature.group,org.eclipse.wst.xml_ui.feature.feature.group,org.eclipse.jpt.common.feature.feature.group,org.eclipse.jpt.jpa.feature.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.m2e.wtp.feature.feature.group

echo "Installing GWT plugin..."
installPlugins http://storage.googleapis.com/gwt-eclipse-plugin/release com.google.gdt.eclipse.suite.e44.feature.feature.group

echo "Installing Android Tools..."
installPlugins https://dl.google.com/eclipse/plugin/4.5 com.android.ide.eclipse.adt.feature.feature.group

echo "Installing GWT SDM debug bridge..."
installPlugins http://p2.sapsailing.com/p2/sdbg com.github.sdbg.feature.feature.group

echo "Installing EasyShell..."
installPlugins http://anb0s.github.io/EasyShell com.tetrade.eclipse.plugins.easyshell.feature.feature.group

echo "Installing latest version of Code Recommenders ..."
updatePlugins http://download.eclipse.org/recommenders/updates/stable/ org.eclipse.recommenders.rcp.feature.feature.group,org.eclipse.recommenders.mylyn.rcp.feature.feature.group,org.eclipse.recommenders.snipmatch.rcp.feature.feature.group,org.eclipse.recommenders.news.rcp.feature.feature.group

echo "Installing latest version of EGit ..."
updatePlugins http://download.eclipse.org/egit/updates org.eclipse.jgit.feature.group,org.eclipse.jgit.http.apache.feature.group,org.eclipse.egit.feature.group,org.eclipse.egit.mylyn.feature.group

echo "Installation completed!"
