#/bin/bash

ARCH=x86_64
PROJECT_HOME=/home/trac/git
MAVEN_SETTINGS=$PROJECT_HOME/settings.xml

ACDIR=`pwd`

gwtcompile=1
testing=1
clean="clean"
offline=0
extra=''

if [ $# -eq 0 ]; then
    echo "buildAndUpdateProduct [-g -t -o -c] [build|install]"
    echo "-g Disable GWT compile, no gwt files will be generated."
    echo "-t Disable tests"
    echo "-o Enable offline mode"
    echo "-c Disable cleaning"
    echo ""
    echo "build: builds the server code but does not install"
    echo "install: builds and installs"
    exit 2
fi

options=':gtoc'
while getopts $options option
do
    case $option in
        g) gwtcompile=0;;
        t) testing=0;;
        o) offline=1;;
        c) clean="";;
        \?) echo "Invalid option"
            exit 2;;
    esac
done

cd $PROJECT_HOME
active_branch=$(git symbolic-ref -q HEAD)
active_branch=`basename $active_branch`
cd $ACDIR
active_server=`basename $ACDIR`

if [ "$active_branch" = "$server_instance" ]; then
    echo "INFO: GIT branch and server branch matching ($active_server)"
else
    echo "ERROR: The current branch ($active_branch) does not match the current server ($active_server)."
    exit
fi

shift $((OPTIND-1))
echo "Starting $@ of server...""

if [ ! -d "plugins" ]; then
    mkdir plugins
fi

# yield build so that we get updated product
cd $PROJECT_HOME/java
if [ $gwtcompile -eq 1 ]; then
    echo "INFO: Compiling GWT"
    rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.*
else
    echo "INFO: GWT Compilation disabled"
    extra="-Pdebug.no-gwt-compile"
fi

if [ $testing -eq 0 ]; then
    echo "INFO: Skipping tests"
    extra="$extra -Dmaven.test.skip=true"
fi

if [ $offline -eq 1 ]; then
    echo "INFO: Activating offline mode"
    extra="$extra -o"
fi

echo "Using following command: mvn $extra -P no-debug.without-proxy -fae -s $MAVEN_SETTINGS $clean install"
mvn $extra -P no-debug.without-proxy -fae -s $MAVEN_SETTINGS $clean install 2>&1 | tee log

# now we have the product and can extract everything from there
cd $ACDIR
p2PluginRepository=$PROJECT_HOME/java/com.sap.sailing.feature.p2build/bin/products/raceanalysis.product.id/linux/gtk/$ARCH
cp -v $p2PluginRepository/configuration/config.ini configuration/
cp -r -v $p2PluginRepository/configuration/org.eclipse.equinox.simpleconfigurator configuration/
cp -v $p2PluginRepository/plugins/*.jar plugins/

echo "Build and installation complete. You may now start the server using ./start"
