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

HEAD_SHA=$(git show-ref --head -s | head -1)
HEAD_DATE=$(date "+%Y%m%d%H%M")
VERSION_INFO="$HEAD_SHA-$active_branch-$HEAD_DATE"

MAVEN_SETTINGS=$PROJECT_HOME/configuration/maven-settings.xml
MAVEN_SETTINGS_PROXY=$PROJECT_HOME/configuration/maven-settings-proxy.xml

p2PluginRepository=$PROJECT_HOME/java/com.sap.sailing.feature.p2build/bin/products/raceanalysis.product.id/linux/gtk/$ARCH

HAS_OVERWRITTEN_TARGET=0
TARGET_SERVER_NAME=$active_branch

gwtcompile=1
testing=1
clean="clean"
offline=0
proxy=0
extra=''

if [ $# -eq 0 ]; then
    echo "buildAndUpdateProduct [-g -t -o -c -m <config> -n <package> -l <port>] [build|install|all|hot-deploy|remote-deploy]"
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
    echo "-s <target server> Name of server you want to use as target for install, hot-deploy or remote-reploy. This overrides default behaviour."
    echo "-w <ssh target> Target for remote-deploy. Must comply with the following format: user@server."
    echo ""
    echo "build: builds the server code using Maven to $PROJECT_HOME (log to $START_DIR/build.log)"
    echo "install: installs product and configuration to $SERVERS_HOME/$active_branch. Overwrites any configuration by using config from branch."
    echo "all: calls build and then install"
    echo ""
    echo "hot-deploy: performs hot deployment of named bundle into OSGi server"
    echo "Example: $0 -n com.sap.sailing.www -l 14888 hot-deploy"
    echo ""
    echo "remote-deploy: performs hot deployment of the java code to a remote server"
    echo "Example: $0 -s dev -w trac@sapsailing.com remote-deploy"
    echo ""
    echo "Active branch is $active_branch"
    echo "Project home is $PROJECT_HOME"
    echo "Server home is $SERVERS_HOME"
    echo "Version info: $VERSION_INFO"
    echo "P2 home is $p2PluginRepository"
    exit 2
fi

echo PROJECT_HOME is $PROJECT_HOME
echo SERVERS_HOME is $SERVERS_HOME
echo BRANCH is $active_branch

options=':gtocpm:n:l:s:w:'
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
        s) TARGET_SERVER_NAME=$OPTARG
           HAS_OVERWRITTEN_TARGET=1;;
        w) REMOTE_SERVER_LOGIN=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done

ACDIR=$SERVERS_HOME/$TARGET_SERVER_NAME
echo INSTALL goes to $ACDIR

shift $((OPTIND-1))

if [[ $@ == "" ]]; then
	echo "You need to specify an action [build|install|all|hot-deploy|remote-deploy]"
	exit 2
fi

if [[ "$@" == "hot-deploy" ]]; then
    # check parameters
    if [[ $OSGI_BUNDLE_NAME == "" ]]; then
        echo "You need to provide -n parameter with bundle name."
        exit 1
    fi

    if [ ! -d $p2PluginRepository/plugins ]; then
        echo "Could not find source directory $p2PluginRepository!"
        exit
    fi

    if [ ! -d $SERVERS_HOME/$active_branch/plugins ]; then
        echo "Could not find target directory $SERVERS_HOME/$active_branch/plugins!"
        exit
    fi

    if [[ $HAS_OVERWRITTEN_TARGET -eq 1 ]]; then
        active_branch=$TARGET_SERVER_NAME
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
    if [[ $TELNET_ACTIVE == "" ]]; then
        # some BSD systems do not support -p
        TELNET_ACTIVE=`netstat -an | grep ".$OSGI_TELNET_PORT"`
    fi

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
    echo -n "Connecting to OSGi server..."
    NC_CMD="nc -t 127.0.0.1 $OSGI_TELNET_PORT"
    echo "OK"
    OLD_BUNDLE_INFORMATION=`echo -n ss | $NC_CMD | grep ${OSGI_BUNDLE_NAME}_`
    BUNDLE_ID=`echo $OLD_BUNDLE_INFORMATION | cut -d " " -f 1`
    OLD_ACTIVATED_NAME=`echo $OLD_BUNDLE_INFORMATION | cut -d " " -f 3`
    echo "Could identify bundle-id $BUNDLE_ID for $OLD_ACTIVATED_NAME"
    read -s -n1 -p "I will now stop and reinstall the bundle mentioned in the line above. Is this right? (y/N): " answer
    case $answer in
    "Y" | "y") echo "Continuing";;
    *) echo "Aborting..."
       exit;;
    esac

    # stop and uninstall
    echo -n stop $BUNDLE_ID | $NC_CMD > /dev/null
    echo -n uninstall $BUNDLE_ID | $NC_CMD > /dev/null

    # make sure bundle is removed
    UNINSTALL_INFORMATION=`echo -n ss | $NC_CMD | grep ${OSGI_BUNDLE_NAME}_`
    if [[ $UNINSTALL_INFORMATION == "" ]]; then
        echo "Uninstall procedure sucessful!"
    else
        echo "Something went wrong during uninstall. Please check error logs."
        exit
    fi

    # now reinstall bundle
    NEW_BUNDLE_ID=`echo -n install file://$SERVERS_HOME/$active_branch/plugins/deploy/${NEW_BUNDLE_BASENAME}.jar | $NC_CMD`
    NEW_BUNDLE_INFORMATION=`echo -n ss | $NC_CMD | grep ${OSGI_BUNDLE_NAME}_`
    NEW_BUNDLE_ID=`echo $NEW_BUNDLE_INFORMATION | cut -d " " -f 1`
    echo "Installed new bundle file://$SERVERS_HOME/$active_branch/plugins/deploy/${NEW_BUNDLE_BASENAME}.jar with id $NEW_BUNDLE_ID"

    # and start
    echo -n start $NEW_BUNDLE_ID | $NC_CMD > /dev/null && sleep 1
    NEW_BUNDLE_STATUS=`echo -n ss | $NC_CMD | grep ${OSGI_BUNDLE_NAME}_ | grep ACTIVE`
    if [[ $NEW_BUNDLE_STATUS == "" ]]; then
        echo "ERROR: Something went wrong with start of bundle. Please check if everything went ok."
        exit
    fi

    echo "Everything seems to be ok. Bundle hot-deployed to server with new id $NEW_BUNDLE_ID"
    exit
fi

echo "Starting $@ of server..."

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
	    extra="$extra -Dmaven.test.skip=true -DskipTests=true"
    else
        extra="$extra -DskipTests=false"
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

    if [ ! -d "$ACDIR/logs" ]; then
        mkdir $ACDIR/logs
    fi

    if [ ! -d "$ACDIR/tmp" ]; then
        mkdir $ACDIR/tmp
    fi

    if [ ! -d "$ACDIR/configuration" ]; then
        mkdir $ACDIR/configuration
    fi

    cd $ACDIR

    rm -rf $ACDIR/plugins/*.*
    rm -rf $ACDIR/org.eclipse.*
    rm -rf $ACDIR/configuration/org.eclipse.*

    if [ ! -f "$ACDIR/env.sh" ]; then
        cp -v $PROJECT_HOME/java/target/env.sh $ACDIR/
        cp -v $PROJECT_HOME/java/target/start $ACDIR/
        cp -v $PROJECT_HOME/java/target/stop $ACDIR/
        cp -v $PROJECT_HOME/java/target/status $ACDIR/
    fi

    if [ ! -f $ACDIR/no-overwrite ]; then
        cp -v $p2PluginRepository/configuration/config.ini configuration/

        mkdir -p configuration/jetty/etc
        cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/jetty.xml configuration/jetty/etc
        cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/jetty-selector.xml configuration/jetty/etc
        cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/jetty-deployer.xml configuration/jetty/etc
        cp -v $PROJECT_HOME/java/target/configuration/jetty/etc/realm.properties configuration/jetty/etc
        cp -v $PROJECT_HOME/java/target/configuration/monitoring.properties configuration/
        cp -v $PROJECT_HOME/configuration/mongodb.cfg $ACDIR/

        cp -v $PROJECT_HOME/java/target/env.sh $ACDIR/
        cp -v $PROJECT_HOME/java/target/start $ACDIR/
        cp -v $PROJECT_HOME/java/target/stop $ACDIR/
        cp -v $PROJECT_HOME/java/target/status $ACDIR/
        cp -v $PROJECT_HOME/java/target/udpmirror $ACDIR/

        cp -v $PROJECT_HOME/java/target/http2udpmirror $ACDIR
        cp -v $PROJECT_HOME/java/target/configuration/logging.properties $ACDIR/configuration
    fi

    cp -r -v $p2PluginRepository/configuration/org.eclipse.equinox.simpleconfigurator configuration/
    cp -v $p2PluginRepository/plugins/*.jar plugins/

    cp -rv $PROJECT_HOME/configuration/native-libraries $ACDIR/

    # Make sure this script is up2date at least for the next run
    cp -v $PROJECT_HOME/configuration/buildAndUpdateProduct.sh $ACDIR/

    # make sure to save the information from env.sh
    . $ACDIR/env.sh

    echo "$VERSION_INFO System:" > $ACDIR/configuration/jetty/version.txt

    # When a server is installed using this script
    # then we no longer define important options in
    # config.ini because this is generated by product
    # installer. Instead we inject these properties
    # using system properties. This works because
    # context.getProperty() searches for system properties
    # if it can't find them in config.ini (framework config)
    sed -i "/mongo.host/d" $ACDIR/configuration/config.ini
    sed -i "/mongo.port/d" $ACDIR/configuration/config.ini
    sed -i "/expedition.udp.port/d" $ACDIR/configuration/config.ini
    sed -i "/replication.exchangeName/d" $ACDIR/configuration/config.ini
    sed -i "/replication.exchangeHost/d" $ACDIR/configuration/config.ini
    sed -i "s/^.*jetty.port.*$/<Set name=\"port\"><Property name=\"jetty.port\" default=\"$SERVER_PORT\"\/><\/Set>/g" $ACDIR/configuration/jetty/etc/jetty.xml

    echo "I have read the following configuration from $ACDIR/env.sh:"
    echo "SERVER_NAME: $SERVER_NAME"
    echo "SERVER_PORT: $SERVER_PORT"
    echo "MEMORY: $MEMORY"
    echo "TELNET_PORT: $TELNET_PORT"
    echo "MONGODB_PORT: $MONGODB_PORT"
    echo "MONGODB_HOST: $MONGODB_HOST"
    echo "EXPEDITION_PORT: $EXPEDITION_PORT"
    echo "REPLICATION_HOST: $REPLICATION_HOST"
    echo "REPLICATION_CHANNEL: $REPLICATION_CHANNEL"
    echo ""

    if [ -f $ACDIR/no-overwrite ]; then
        echo "ATTENTION: I found the file $ACDIR/no-overwrite. This means that I did NOT use env.sh from this branch."
    fi
    echo "Installation complete. You may now start the server using ./start"
fi

if [[ "$@" == "remote-deploy" ]]; then
    SERVER=$TARGET_SERVER_NAME
    echo "Will deploy server $SERVER"

    SSH_CMD="ssh $REMOTE_SERVER_LOGIN"
    SCP_CMD="scp -r"

    REMOTE_HOME=`ssh $REMOTE_SERVER_LOGIN 'echo $HOME/servers'`
    REMOTE_SERVER="$REMOTE_HOME/$SERVER"

    read -s -n1 -p "I will deploy the current GIT branch to $REMOTE_SERVER_LOGIN:$REMOTE_SERVER. Is this correct (y/n)? " answer
    case $answer in
    "Y" | "y") OK=1;;
    *) echo "Aborting... nothing has been changed on remote server!"
    exit;;
    esac

    $SSH_CMD "test -d $REMOTE_SERVER/plugins"
    if [[ $? -eq 1 ]]; then
        echo "Did not find directory $REMOTE_SERVER/plugins - assuming empty server that needs to be initialized! Using data from $PROJECT_HOME"

        $SSH_CMD "mkdir -p $REMOTE_SERVER/plugins"
        $SSH_CMD "mkdir -p $REMOTE_SERVER/logs"
        $SSH_CMD "mkdir -p $REMOTE_SERVER/tmp"
        $SSH_CMD "mkdir -p $REMOTE_SERVER/configuration/jetty/etc"

        $SCP_CMD $p2PluginRepository/configuration/config.ini $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/
        $SCP_CMD $PROJECT_HOME/java/target/configuration/jetty/etc/jetty.xml $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/jetty/etc
        $SCP_CMD $PROJECT_HOME/java/target/configuration/jetty/etc/realm.properties $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/jetty/etc
        $SCP_CMD $PROJECT_HOME/java/target/configuration/monitoring.properties $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/
 
        $SCP_CMD $PROJECT_HOME/java/target/env.sh $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/
        $SCP_CMD $PROJECT_HOME/java/target/start $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/
        $SCP_CMD $PROJECT_HOME/java/target/stop $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/
        $SCP_CMD $PROJECT_HOME/java/target/status $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/
        $SCP_CMD $PROJECT_HOME/java/target/udpmirror $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/
 
        $SCP_CMD $PROJECT_HOME/java/target/http2udpmirror $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/
        $SCP_CMD $PROJECT_HOME/java/target/configuration/logging.properties $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/
    fi

    echo ""
    echo "Starting deployment to $REMOTE_HOME/$SERVER..."

    $SSH_CMD "rm -rf $REMOTE_SERVER/plugins/*.*"
    $SSH_CMD "rm -rf $REMOTE_SERVER/org.eclipse*.*"
    $SSH_CMD "rm -rf $REMOTE_SERVER/configuration/org.eclipse*.*"

    $SCP_CMD $p2PluginRepository/configuration/org.eclipse.equinox.simpleconfigurator $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/
    $SCP_CMD $p2PluginRepository/plugins/*.jar $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/plugins/

    echo "$VERSION_INFO System: remotedly-deployed" > /tmp/version-remote-deploy.txt
    $SCP_CMD /tmp/version-remote-deploy.txt $REMOTE_SERVER_LOGIN:$REMOTE_SERVER/configuration/jetty/version.txt
    rm /tmp/version-remote-deploy.txt

    echo "Deployed successfully. I did NOT change any configuration (no env.sh or config.ini or jetty.xml adaption), only code!"

    read -s -n1 -p "Do you want me to restart the remote server (y/n)? " answer
    case $answer in
    "Y" | "y") OK=1;;
    *) echo "Aborting... deployment should be ready by now!"
    exit;;
    esac

    echo ""
    $SSH_CMD "cd $REMOTE_SERVER && bash -l -c $REMOTE_SERVER/stop"
    $SSH_CMD "cd $REMOTE_SERVER && bash -l -c $REMOTE_SERVER/start"

    echo "Restarted remote server. Please check."
fi

if [[ "$@" == "deploy-startpage" ]]; then
    TARGET_DIR_STARTPAGE=$ACDIR/tmp/jetty-0.0.0.0-8889-bundlefile-_-any-/webapp/
    read -s -n1 -p "Copying $PROJECT_HOME/java/com.sap.sailing.www/index.html to $TARGET_DIR_STARTPAGE - is this ok (y/n)?" answer
    case $answer in
    "Y" | "y") OK=1;;
    *) echo "Aborting... nothing has been changed for startpage!"
    exit;;
    esac

    cp $PROJECT_HOME/java/com.sap.sailing.www/index.html $TARGET_DIR_STARTPAGE
    echo "OK"
fi

echo "Operation finished at `date`"
