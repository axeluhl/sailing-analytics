#/bin/bash

PROJECT_HOME=~/Projects/sailing/code

ACDIR=`pwd`

gwtcompile=1
testing=1
clean="clean"
offline=0
extra=''

options='gtohc'
while getopts $options option
do
    case $option in
        g) gwtcompile=0;;
        t) testing=0;;
        o) offline=1;;
        c) clean="";;
        h) echo "dir-update -g -t -o -c"
            echo "-g Disable GWT compile, no gwt files will be generated."
            echo "-t Disable tests"
            echo "-o Enable offline mode"
            echo "-c Disable cleaning"
            exit;;
    esac
done

cp -r ../code/java/target/* .

if [ ! -d "plugins" ]; then
    mkdir plugins
fi

# yield build so that we get updated product
cd ../code/java
if [ $gwtcompile -eq 1 ]; then
    echo "INFO: Compiling GWT"
    rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.*
else
    echo "INFO: GWT Compilation disabled"
    extra="-Pdebug.no-gwt-compile"

    if [ ! -d "com.sap.sailing.gwt.ui/com.sap.sailing.Spectator" ]; then
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.Spectator
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.RaceBoard
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.LeaderboardEditing
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.AdminConsole
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.Leaderboard
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.UserManagement
        mkdir com.sap.sailing.gwt.ui/com.sap.sailing.TvView
    fi
fi

if [ $testing -eq 0 ]; then
    echo "INFO: Skipping tests"
    extra="$extra -Dmaven.test.skip=true"
fi

if [ $offline -eq 1 ]; then
    echo "INFO: Activating offline mode"
    extra="$extra -o"
fi

echo "Using following command: mvn $extra -fae $clean install"
mvn $extra -fae $clean install

# now we have the product and can extract everything from there
echo "Switching back to $ACDIR"
cd $ACDIR

p2PluginRepository=$PROJECT_HOME/java/com.sap.sailing.feature.p2build/bin/products/raceanalysis.product.id/linux/gtk/x86
cp -v $p2PluginRepository/configuration/config.ini configuration/
cp -r -v $p2PluginRepository/configuration/org.eclipse.equinox.simpleconfigurator configuration/
cp -v $p2PluginRepository/plugins/*.jar plugins/
echo Done.

cp custom/config.ini configuration/
cp custom/start .
cp custom/monitoring.properties configuration/

if [ $gwtcompile -eq 0 ]; then
    cd ../code/java
    if [ -d "com.sap.sailing.gwt.ui/com.sap.sailing.Spectator" ]; then
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.Spectator
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.RaceBoard
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.LeaderboardEditing
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.AdminConsole
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.Leaderboard
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.UserManagement
        rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.TvView
    fi
    cd -
fi

echo ""
echo "Make sure to start MongoDB before starting server"
