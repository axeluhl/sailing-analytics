#!/bin/bash

# You need an installation of the Photon release of "Eclipse IDE for Eclipse Committers" matching you OS and JDK (32 vs 64 Bit):
# http://www.eclipse.org/downloads/packages/eclipse-ide-eclipse-committers/photonr

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
installPlugins http://download.eclipse.org/releases/photon/ org.eclipse.wst.web_ui.feature.feature.group,org.eclipse.jst.web_ui.feature.feature.group,org.eclipse.wst.xml_ui.feature.feature.group,org.eclipse.jpt.common.feature.feature.group,org.eclipse.jpt.jpa.feature.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.m2e.wtp.feature.feature.group

echo "Installing GWT plugin..."
installPlugins http://storage.googleapis.com/gwt-eclipse-plugin/v3/release com.gwtplugins.eclipse.suite.v3.feature.feature.group

echo "Installing Android Tools..."
installPlugins https://dl-ssl.google.com/android/eclipse com.android.ide.eclipse.adt.feature.feature.group

echo "Installing GWT SDM debug bridge..."
installPlugins http://p2.sapsailing.com/p2/sdbg com.github.sdbg.feature.feature.group

echo "Installing EasyShell..."
installPlugins http://anb0s.github.io/EasyShell de.anbos.eclipse.easyshell.feature.feature.group

echo "Installing BIRT charts (requirement for MAT)..."
installPlugins http://download.eclipse.org/birt/update-site/4.6 org.eclipse.birt.chart.feature.group

echo "Installing Memory Analyzer..."
installPlugins http://download.eclipse.org/releases/photon org.eclipse.mat.feature.feature.group,org.eclipse.mat.chart.feature.feature.group

echo "Installing latest version of Code Recommenders..."
updatePlugins http://download.eclipse.org/recommenders/updates/stable/ org.eclipse.recommenders.rcp.feature.feature.group,org.eclipse.recommenders.mylyn.rcp.feature.feature.group,org.eclipse.recommenders.snipmatch.rcp.feature.feature.group,org.eclipse.recommenders.news.rcp.feature.feature.group

echo "Installing latest version of EGit..."
updatePlugins http://download.eclipse.org/egit/updates org.eclipse.jgit.feature.group,org.eclipse.jgit.http.apache.feature.group,org.eclipse.egit.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.gitflow.feature.feature.group

# Currently not needed because Eclipse Photon already provides current versions
# echo "Installing latest version of Mylyn ..."
# updatePlugins http://download.eclipse.org/mylyn/releases/latest org.eclipse.mylyn_feature.feature.group,org.eclipse.mylyn.bugzilla_feature.feature.group,org.eclipse.mylyn.builds.feature.group,org.eclipse.mylyn.commons.feature.group,org.eclipse.mylyn.commons.identity.feature.group,org.eclipse.mylyn.commons.notifications.feature.group,org.eclipse.mylyn.commons.repositories.feature.group,org.eclipse.mylyn.commons.repositories.http.feature.group,org.eclipse.mylyn.context_feature.feature.group,org.eclipse.mylyn.discovery.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.hudson.feature.group,org.eclipse.mylyn.java_feature.feature.group,org.eclipse.mylyn.monitor.feature.group,org.eclipse.mylyn.reviews.feature.feature.group,org.eclipse.mylyn.team_feature.feature.group,org.eclipse.mylyn.versions.feature.group,org.eclipse.mylyn.wikitext_feature.feature.group

echo "Installing SAP JVM Tools (profiler) ..."
installPlugins https://tools.hana.ondemand.com/photon com.sap.jvm.profiling.feature.group

echo "Installation completed!"
