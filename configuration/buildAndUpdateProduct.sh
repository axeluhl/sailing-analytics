#/bin/bash

# this holds for default installation
USER_HOME=~
PROJECT_HOME=$USER_HOME/git

# x86 or x86_64 should work for most cases
ARCH=x86_64

ACDIR=`pwd`

if [[ "$ACDIR" == "$PROJECT_HOME/configuration" ]] || [[ "$ACDIR" == "$PROJECT_HOME" ]]; then
    echo "Please DO NOT run this script from $PROJECT_HOME/configuration or $PROJECT_HOME. Your directory should differ from the GIT repo."
    exit 2
fi

# needed for maven to work correctly
source $USER_HOME/.bash_profile

MAVEN_SETTINGS=$ACDIR/maven-settings.xml

gwtcompile=1
testing=1
clean="clean"
offline=0
extra=''

if [ $# -eq 0 ]; then
    echo "buildAndUpdateProduct [-g -t -o -c] [build|install|all]"
    echo ""
    echo "-g Disable GWT compile, no gwt files will be generated, old ones will be preserved."
    echo "-t Disable tests"
    echo "-o Enable offline mode (does not work for tycho surefire plugin)"
    echo "-c Disable cleaning (use only if you are sure that no java file has changed)"
    echo ""
    echo "build: builds the server code using Maven to $PROJECT_HOME"
    echo "install: installs product and configuration to $ACDIR. Overwrites any configuration by using config from branch."
    echo "all: calls build and then install"
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

if [[ "$active_branch" == "$active_server" ]]; then
    echo "INFO: GIT branch and server branch matching ($active_server)"
else
    echo "ERROR: The current branch ($active_branch) does not match the current server ($active_server)."
    exit
fi

shift $((OPTIND-1))

if [[ $@ == "" ]]; then
	echo "You need to specify an action [build|install]"
	exit 2
fi

echo "Starting $@ of server..."

if [ ! -d "$ACDIR/plugins" ]; then
    mkdir $ACDIR/plugins
fi

if [ ! -d "$ACDIR/configuration" ]; then
    mkdir $ACDIR/configuration
fi

if [[ "$@" == "build" ]] || [[ "$@" == "all" ]]; then
	# yield build so that we get updated product
	cd $PROJECT_HOME/java
	if [ $gwtcompile -eq 1 ]; then
	    echo "INFO: Compiling GWT (rm -rf com.sap.sailing.gwt.ui/com.sap.sailing.*)"
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
	mvn $extra -P no-debug.without-proxy -fae -s $MAVEN_SETTINGS $clean install 2>&1 | tee $ACDIR/build.log

	echo "Build complete. Do not forget to install product..."
fi

if [[ "$@" == "install" ]] || [[ "$@" == "all" ]]; then

	cd $ACDIR

    rm -rf $ACDIR/plugins/*.*
    rm -rf $ACDIR/org.eclipse.*
    rm -rf $ACDIR/configuration/org.eclipse.*

    p2PluginRepository=$PROJECT_HOME/java/com.sap.sailing.feature.p2build/bin/products/raceanalysis.product.id/linux/gtk/$ARCH
    cp -v $p2PluginRepository/configuration/config.ini configuration/
    cp -r -v $p2PluginRepository/configuration/org.eclipse.equinox.simpleconfigurator configuration/
    cp -v $p2PluginRepository/plugins/*.jar plugins/

    mkdir -p configuration/jetty/etc
    cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/jetty.xml configuration/jetty/etc
    cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/realm.properties configuration/jetty/etc
    cp -v $PROJECT_HOME/java/target/configuration/monitoring.properties configuration/

    # Make sure mongodb configuration is active
    cp -v $PROJECT_HOME/configuration/mongodb.cfg .
    cp -rv $PROJECT_HOME/configuration/native-libraries .

    # Make sure this script is up2date
    cp -v $PROJECT_HOME/configuration/buildAndUpdateProduct.sh .

	echo "Installation complete. You may now start the server using ./start"
fi
