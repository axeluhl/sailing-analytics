#!/bin/bash
set -o functrace

find_project_home () 
{
    if [[ $1 == '/' ]] || [[ $1 == "" ]]; then
        echo ""
        return 0
    fi

    if [ ! -d "$1/.git" ]; then
        PARENT_DIR=`cd $1/..;pwd`
        echo $(find_project_home $PARENT_DIR)
        return 0
    fi

    echo $1 | sed -e 's/\/cygdrive\/\([a-zA-Z]\)/\1:/'
}

# this holds for default installation
USER_HOME=~
START_DIR=`pwd`

if [ "$PROJECT_HOME" = "" ]; then
    PROJECT_HOME=$(find_project_home $START_DIR)
fi

# if project_home is still empty we could not determine any suitable directory
if [[ $PROJECT_HOME == "" ]]; then
    echo "Could neither determine nor get PROJECT_HOME. Please provide it by setting an environment variable with this name."
    exit
fi

if [ "$SERVERS_HOME" = "" ]; then
  SERVERS_HOME=`echo "$USER_HOME/servers" | sed -e 's/\/cygdrive\/\([a-zA-Z]\)/\1:/'`
fi

# x86 or x86_64 should work for most cases
ARCH=x86_64
START_DIR=`pwd`

# needed for maven on sapsailing.com to work correctly
if [ -f $USER_HOME/.bash_profile ]; then
    source $USER_HOME/.bash_profile
fi

cd $PROJECT_HOME
active_branch=$(git symbolic-ref -q HEAD)
active_branch=`basename $active_branch`

ACDIR=$SERVERS_HOME/$active_branch

MAVEN_SETTINGS=$PROJECT_HOME/configuration/maven-settings.xml
MAVEN_SETTINGS_PROXY=$PROJECT_HOME/configuration/maven-settings-proxy.xml

p2PluginRepository=$PROJECT_HOME/java/com.sap.sailing.feature.p2build/bin/products/raceanalysis.product.id/linux/gtk/$ARCH

gwtcompile=1
testing=1
clean="clean"
offline=0
proxy=0
extra=''

if [ $# -eq 0 ]; then
    echo "buildAndUpdateProduct [-g -t -o -c -m <config> -n <package> -l <port>] [build|install|all|hot-deploy]"
    echo ""
    echo "-g Disable GWT compile, no gwt files will be generated, old ones will be preserved."
    echo "-t Disable tests"
    echo "-o Enable offline mode (does not work for tycho surefire plugin)"
    echo "-c Disable cleaning (use only if you are sure that no java file has changed)"
    echo "-p Enable proxy mode (overwrites file specified by -m)"
    echo "-m <path to file> Specify alternate maven configuration (possibly has side effect on proxy setting)"
    echo "-n <package name> Name of the bundle you want to hot deploy. Needs fully qualified name like"
    echo "                  com.sap.sailing.monitoring. Only works if there is a fully built server available."
    echo "-l <telnet port>  Telnet port the OSGi server is running. Optional but enables fully automatic hot-deploy."
    echo ""
    echo "build: builds the server code using Maven to $PROJECT_HOME (log to $START_DIR/build.log)"
    echo "install: installs product and configuration to $SERVERS_HOME/$active_branch. Overwrites any configuration by using config from branch."
    echo "all: calls build and then install"
    echo "hot-deploy: performs hot deployment of named bundle into OSGi server"
    echo ""
    echo "Active branch is $active_branch"
    echo "Project home is $PROJECT_HOME"
    echo "Server home is $SERVERS_HOME"
    echo "P2 home is $p2PluginRepository"
    exit 2
fi

echo PROJECT_HOME is $PROJECT_HOME
echo SERVERS_HOME is $SERVERS_HOME
echo BRANCH is $active_branch

options=':gtocpm:n:l:'
while getopts $options option
do
    case $option in
        g) gwtcompile=0;;
        t) testing=0;;
        o) offline=1;;
        c) clean="";;
        p) proxy=1;;
        m) MAVEN_SETTINGS=$OPTARG;;
        n) OSGI_BUNDLE_NAME=$OPTARG;;
        l) OSGI_TELNET_PORT=$OPTARG;;
        \?) echo "Invalid option"
            exit 2;;
    esac
done

shift $((OPTIND-1))

if [[ $@ == "" ]]; then
	echo "You need to specify an action [build|install|all|hot-deploy]"
	exit 2
fi

if [[ "$@" == "hot-deploy" ]]; then
    # check parameters
    if [[ $OSGI_BUNDLE_NAME == "" ]]; then
        echo "You need to provide -n parameter with bundle name."
        exit 1
    fi

    # locate old bundle
    BUNDLE_COUNT=`find $SERVERS_HOME/$active_branch/plugins -maxdepth 1 -name "${OSGI_BUNDLE_NAME}_*.jar" | wc -l`
    OLD_BUNDLE=`find $SERVERS_HOME/$active_branch/plugins -maxdepth 1 -name "${OSGI_BUNDLE_NAME}_*.jar"`
    if [[ $OLD_BUNDLE == "" ]] || [[ $BUNDLE_COUNT -ne 1 ]]; then
        echo "ERROR: Could not find any bundle named $OSGI_BUNDLE_NAME ($BUNDLE_COUNT). Perhaps your name is misspelled or you have no build?"
        exit
    fi

    OLD_BUNDLE_BASENAME=`basename $OLD_BUNDLE .jar`
    OLD_BUNDLE_VERSION=${OLD_BUNDLE_BASENAME#*_}

    echo "OLD bundle is $OSGI_BUNDLE_NAME with version $OLD_BUNDLE_VERSION"

    # locate new bundle
    NEW_BUNDLE=`find $p2PluginRepository/plugins -maxdepth 1 -name "${OSGI_BUNDLE_NAME}_*.jar"`
    NEW_BUNDLE_BASENAME=`basename $NEW_BUNDLE .jar`
    NEW_BUNDLE_VERSION=${NEW_BUNDLE_BASENAME#*_}
    echo "NEW bundle is $OSGI_BUNDLE_NAME with version $NEW_BUNDLE_VERSION"

    if [[ $NEW_BUNDLE_VERSION == $OLD_BUNDLE_VERSION ]]; then
        echo ""
        echo "WARNING: Bundle versions do not differ. Update not needed."
    fi

    read -s -n1 -p "Do you really want to hot-deploy bundle $OSGI_BUNDLE_NAME to $SERVERS_HOME/$active_branch? (y/N): " answer
    case $answer in
    "Y" | "y") echo "Continuing";;
    *) echo "Aborting..."
       exit;;
    esac

    # deploy new bundle physically
    mkdir -p $SERVERS_HOME/$active_branch/plugins/deploy
    cp $NEW_BUNDLE $SERVERS_HOME/$active_branch/plugins/deploy
    echo "Copied ${NEW_BUNDLE_BASENAME}.jar to $SERVERS_HOME/$active_branch/plugins/deploy"

    # check telnet port connection
    TELNET_ACTIVE=`netstat -tlnp 2>/dev/null | grep ":$OSGI_TELNET_PORT"`
    if [[ $OSGI_TELNET_PORT == "" ]] || [[ $TELNET_ACTIVE == "" ]]; then
        echo ""
        echo "ERROR: Could not find any process running on port $OSGI_TELNET_PORT. Make sure your server has been started with -console $OSGI_TELNET_PORT"
        echo "I've already deployed bundle to $SERVERS_HOME/$active_branch/plugins/deploy/${NEW_BUNDLE_BASENAME}.jar"
        echo "You can now install it yourself by issuing the following commands:"
        echo ""
        echo "osgi> ss $OSGI_BUNDLE_NAME"
        echo "21   ACTIVE   $OLD_BUNDLE_BASENAME"
        echo "osgi> stop 21"
        echo "osgi> uninstall 21"
        echo "osgi> install file://$SERVERS_HOME/$active_branch/plugins/deploy/${NEW_BUNDLE_BASENAME}.jar"
        echo "osgi> ss $OSGI_BUNDLE_NAME"
        echo "71   INSTALLED   $NEW_BUNDLE_BASENAME"
        echo "osgi> start 71"
        exit
    fi

    # first get bundle ID
    NC_CMD="nc -t 127.0.0.1 $OSGI_TELNET_PORT"
    OLD_BUNDLE_INFORMATION=`echo -n ss | $NC_CMD | grep $OSGI_BUNDLE_NAME`
    BUNDLE_ID=`echo $OLD_BUNDLE_INFORMATION | cut -d " " -f 1`
    OLD_ACTIVATED_NAME=`echo $OLD_BUNDLE_INFORMATION | cut -d " " -f 3`
    echo "Could identify bundle-id $BUNDLE_ID for $OLD_ACTIVATED_NAME"

    # stop and uninstall
    echo -n stop $BUNDLE_ID | $NC_CMD > /dev/null
    echo -n uninstall $BUNDLE_ID | $NC_CMD > /dev/null

    # make sure bundle is removed
    UNINSTALL_INFORMATION=`echo -n ss | $NC_CMD | grep $OSGI_BUNDLE_NAME`
    if [[ $UNINSTALL_INFORMATION == "" ]]; then
        echo "Uninstall procedure sucessful!"
    else
        echo "Something went wrong during uninstall. Please check error logs."
        exit
    fi

    # now reinstall bundle
    NEW_BUNDLE_ID=`echo -n install file://$SERVERS_HOME/$active_branch/plugins/deploy/${NEW_BUNDLE_BASENAME}.jar | $NC_CMD`
    NEW_BUNDLE_INFORMATION=`echo -n ss | $NC_CMD | grep $OSGI_BUNDLE_NAME`
    NEW_BUNDLE_ID=`echo $NEW_BUNDLE_INFORMATION | cut -d " " -f 1`
    echo "Installed new bundle file://$SERVERS_HOME/$active_branch/plugins/deploy/${NEW_BUNDLE_BASENAME}.jar with id $NEW_BUNDLE_ID"

    # and start
    echo -n start $NEW_BUNDLE_ID | $NC_CMD > /dev/null && sleep 1
    NEW_BUNDLE_STATUS=`echo -n ss | $NC_CMD | grep $OSGI_BUNDLE_NAME | grep ACTIVE`
    if [[ $NEW_BUNDLE_STATUS == "" ]]; then
        echo "ERROR: Something went wrong with start of bundle. Please check if everything went ok."
        exit
    fi

    echo "Everything seems to be ok. Bundle hot-deployed to server with new id $NEW_BUNDLE_ID"
    exit
fi

echo "Starting $@ of server (maven configuration set to $MAVEN_SETTINGS)..."

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

	if [ $proxy -eq 1 ]; then
	    echo "INFO: Activating proxy profile"
	    extra="$extra -P no-debug.with-proxy"
	    MAVEN_SETTINGS=$MAVEN_SETTINGS_PROXY
	else
	    extra="$extra -P no-debug.without-proxy"
	fi

	echo "Using following command: mvn $extra -fae -s $MAVEN_SETTINGS $clean install"
	mvn $extra -fae -s $MAVEN_SETTINGS $clean install 2>&1 | tee $START_DIR/build.log

	echo "Build complete. Do not forget to install product..."
fi

if [[ "$@" == "install" ]] || [[ "$@" == "all" ]]; then

    read -s -n1 -p "Currently branch $active_branch is active and I will deploy to $ACDIR. Do you want to proceed with $@ (y/N): " answer
    case $answer in
    "Y" | "y") echo "Continuing";;
    *) echo "Aborting..."
       exit;;
    esac

    if [ ! -d $ACDIR ]; then
        echo "Could not find directory $ACDIR - perhaps you are on a wrong branch?"
        exit
    fi

    # secure current state so that it can be reused if something goes wrong
    if [ -f "$ACDIR/backup-binaries.tar.gz" ]; then
        rm -f $ACDIR/backup-binaries.tar.gz
    fi

    tar cvzf $ACDIR/backup-binaries.tar.gz $ACDIR/plugins $ACDIR/configuration

    if [ ! -d "$ACDIR/plugins" ]; then
        mkdir $ACDIR/plugins
    fi

    if [ ! -d "$ACDIR/configuration" ]; then
        mkdir $ACDIR/configuration
    fi

    # seems that initial scripts not there
    if [ ! -f "$ACDIR/start" ]; then
        cp -v $PROJECT_HOME/java/target/start $ACDIR
        cp -v $PROJECT_HOME/java/target/http2udpmirror $ACDIR
        cp -v $PROJECT_HOME/java/target/configuration/logging.properties $ACDIR/configuration
    fi

    cd $ACDIR

    rm -rf $ACDIR/plugins/*.*
    rm -rf $ACDIR/org.eclipse.*
    rm -rf $ACDIR/configuration/org.eclipse.*

    rm -rf $ACDIR/start
    rm -rf $ACDIR/stop

    cp -v $p2PluginRepository/configuration/config.ini configuration/
    cp -r -v $p2PluginRepository/configuration/org.eclipse.equinox.simpleconfigurator configuration/
    cp -v $p2PluginRepository/plugins/*.jar plugins/

    mkdir -p configuration/jetty/etc
    cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/jetty.xml configuration/jetty/etc
    cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/realm.properties configuration/jetty/etc
    cp -v $PROJECT_HOME/java/target/configuration/monitoring.properties configuration/

    # Make sure mongodb configuration is active
    cp -v $PROJECT_HOME/configuration/mongodb.cfg $ACDIR/
    cp -rv $PROJECT_HOME/configuration/native-libraries $ACDIR/

    # Make sure this script is up2date at least for the next run
    cp -v $PROJECT_HOME/configuration/buildAndUpdateProduct.sh $ACDIR/

    cp -v $PROJECT_HOME/java/target/start $ACDIR/
    cp -v $PROJECT_HOME/java/target/stop $ACDIR/

    echo "Installation complete. You may now start the server using ./start"
fi
