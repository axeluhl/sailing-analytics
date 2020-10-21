package com.sap.sailing.landscape;

import com.jcraft.jsch.ChannelSftp;
import com.sap.sse.landscape.ReleaseRepository;

/**
 * Fields acceptable as user data, either for launching an AWS instance with these user data which then get appended to
 * an {@code env.sh} file by the {@code sailing} startup / init script, or for appending to an {@code env.sh} file
 * programmatically, e.g., through a {@link ChannelSftp}.
 * <p>
 * 
 * The enum literals correspond in spelling and capitalization precisely what the {@code start} script that launches a
 * server process expects to find as environment variables. Therefore, the {@link Enum#name()} method can be used to
 * produce those variable names.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public enum UserData {
    /**
     * The user data variable to use to specify the release to install and run on the host. See also
     * {@link ReleaseRepository} and {@link #getReleaseUserData}.
     */
    INSTALL_FROM_RELEASE,
    
    /**
     * The user data variable used to specify the MongoDB connection URI
     */
    MONGODB_URI,
    
    /**
     * The user data variable used to define the name of the replication channel to which this master node
     * will send its operations bound for its replica nodes.
     */
    REPLICATION_CHANNEL,
    
    /**
     * The user data variable used to define the server's name. This is relevant in particular for the user group
     * created/used for all new server-specific objects such as the {@code SERVER} object itself. The group's
     * name is constructed by appending "-server" to the server name.
     */
    SERVER_NAME,
    
    /**
     * User data variable that defines one or more comma-separated e-mail addresses to which a notification will
     * be sent after the server has started successfully.
     */
    SERVER_STARTUP_NOTIFY,
    
    /**
     * User data variable defining the environment file (stored at {@code http://releases.sapsailing.com/environments})
     * which provides default combinations of variables
     */
    USE_ENVIRONMENT;
}
