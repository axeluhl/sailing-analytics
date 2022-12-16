package com.sap.sse.landscape;

import com.jcraft.jsch.ChannelSftp;

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
public enum DefaultProcessConfigurationVariables implements ProcessConfigurationVariable {
    /**
     * The user data variable to use to specify the release to install and run on the host. See also
     * {@link ReleaseRepository} and {@link #getReleaseUserData}.
     */
    INSTALL_FROM_RELEASE,
    
    /**
     * The amount of memory to grant to the process. Expected in the format a Java VM would accept for its {@code -Xmx}
     * VM parameter, e.g., {@code 2500m} or {@code 4g}. If provided, this takes precedence over the
     * {@link #TOTAL_MEMORY_SIZE_FACTOR} variable. If neither this nor {@link #TOTAL_MEMORY_SIZE_FACTOR} are provided,
     * a default size is computed based on the physical RAM available, assuming this process is the only application
     * process running on the host.
     */
    MEMORY,
    
    /**
     * Will be superseded by the {@link #MEMORY} variable and is used only to compute a default in case no explicit
     * {@link #MEMORY} value is provided. The factor by which the total physical memory size (minus some space for the
     * operating system) is greater than the memory to award to the process. Example: {@code 4} would mean that if the
     * host has 66GB of physical RAM of which 2GB are reserved for the operating system then 16GB would be reserved for
     * the process ((66GB-2GB)/4). The factor is an approximation for how many such processes will fit into the physical
     * RAM at the same time. If neither this nor {@link #MEMORY} are provided, a default size is computed based on the
     * physical RAM available, assuming this process is the only application process running on the host. (Obviously, if
     * the resulting memory size for the process is used as a Java VM's heap size, more memory than just the heap size
     * will be consumed by the Java VM process in total. Yet, as an approximation this is better than nothing.)
     */
    TOTAL_MEMORY_SIZE_FACTOR,
    
    /**
     * Can be used to pass additional VM arguments to the Java VM used to run the process configured by these variables.
     * If you specify a value, make sure to always include the previous value in the new value. Example value:
     * <pre>
     *   "${ADDITIONAL_JAVA_ARGS} -Dan.additional.system.property=theValue"
     * </pre>
     */
    ADDITIONAL_JAVA_ARGS,
    
    /**
     * The user data variable used to specify the MongoDB connection URI
     */
    MONGODB_URI,
    
    /**
     * The user data variable used to define the name of the RabbitMQ exchange to which this master node
     * will send its operations bound for its replica nodes. The replica-side counterpart for this is
     * {@link #REPLICATE_MASTER_EXCHANGE_NAME}.
     */
    REPLICATION_CHANNEL,

    /**
     * Variable name for the hostname or IP address of the RabbitMQ node that this master process will use for outbound replication.
     */
    REPLICATION_HOST,
    
    /**
     * Variable name for the port used by this master process to connect to RabbitMQ for outbound replication. Using {@code 0}
     * will use the default port as encoded in the RabbitMQ driver.
     */
    REPLICATION_PORT,
    
    /**
     * The user data variable used to define the server's name. This is relevant in particular for the user group
     * created/used for all new server-specific objects such as the {@code SERVER} object itself. The group's
     * name is constructed by appending "-server" to the server name. See also {@link ServerInfo}.
     */
    SERVER_NAME,
    
    /**
     * The port on which the built-in web server of an application server process can be reached using HTTP
     */
    SERVER_PORT,
    
    /**
     * The port on which the OSGi console of a server process can be reached
     */
    TELNET_PORT,
    
    /**
     * User data variable that defines one or more comma-separated e-mail addresses to which a notification will
     * be sent after the server has started successfully.
     */
    SERVER_STARTUP_NOTIFY,
    
    /**
     * User data variable defining the environment file (stored at {@code http://releases.sapsailing.com/environments})
     * which provides default combinations of variables
     */
    USE_ENVIRONMENT,
    
    /**
     * Variable for the host name or IP address where a replica can reach the master node in order to
     * request the initial load, register, un-register, and send operations for reverse replication to.
     * The value is always combined with that of the {@link #REPLICATE_MASTER_SERVLET_PORT} variable which
     * provides the port for this communication.
     */
    REPLICATE_MASTER_SERVLET_HOST,
    
    /**
     * Variable for the port number where a replica can reach the master node in order to
     * request the initial load, register, un-register, and send operations for reverse replication to.
     * The value is always combined with that of the {@link #REPLICATE_MASTER_SERVLET_HOST} variable which
     * provides the host name / IP address for this communication.
     */
    REPLICATE_MASTER_SERVLET_PORT,
    
    /**
     * Variable describing the name of the RabbitMQ exchange to which the master sends operations for fan-out
     * distribution to all replicas, and that therefore a replica has to attach a queue to in order to receive
     * those operations. Specified on a replica. The master-side counterpart is {@link #REPLICATION_CHANNEL}.
     */
    REPLICATE_MASTER_EXCHANGE_NAME,
    
    /**
     * Variable for the RabbitMQ host name that this replica will connect to in order to connect a queue to the
     * fan-out exchange whose name is provided by the {@link #REPLICATE_MASTER_EXCHANGE_NAME} variable. Used
     * in conjunction with the {@link #REPLICATE_MASTER_QUEUE_PORT} variable.
     */
    REPLICATE_MASTER_QUEUE_HOST,
    
    /**
     * Variable for the RabbitMQ port that this replica will connect to in order to connect a queue to the fan-out
     * exchange whose name is provided by the {@link #REPLICATE_MASTER_EXCHANGE_NAME} variable. Defaults to 0 which
     * instructs the driver to use the Rabbit default port (usually 5672) for connecting. Used in conjunction with the
     * {@link #REPLICATE_MASTER_QUEUE_HOST} variable.
     */
    REPLICATE_MASTER_QUEUE_PORT,
    
    /**
     * If set to any non-empty value, e.g., {@code true}, this will cause the {@link #REPLICATE_ON_START} property to
     * be set to the full set of replicables known by the solution. Furthermore, it will cause the {@link #REPLICATION_CHANNEL}
     * variable to be set to {@code ${SERVER_NAME}-${INSTANCE_NAME}} instead of the default {@code ${SERVER_NAME}}.<p>
     * 
     * In short, setting this to a non-empty value will make the application launched into a replica in a regular application
     * replica set. Contrast this with a master process of a regular application replica set which is a "partial replica"
     * with regards to, e.g., the security service. For those, this property should not be set.
     */
    AUTO_REPLICATE,
    
    /**
     * Variable that specifies the IDs (basically the fully-qualified class names) of those {@code Replicable}s to
     * start replicating when the server process starts. The process using this will become a replica for those
     * replicables specified with this variable, and it will replicate the master node described by
     * {@link #REPLICATE_MASTER_SERVLET_HOST} and {@link #REPLICATE_MASTER_SERVLET_PORT} and receive the operation
     * feed through the RabbitMQ exchange configured by {@link #REPLICATE_MASTER_EXCHANGE_NAME}.
     */
    REPLICATE_ON_START,
    
    /**
     * The user data variable used to specify which bearer token to use to authenticate at the master
     * in case this is to become a replica of some sort, e.g., replicating the {@code SecurityService}
     * and the {@code SharedSailingData} service.
     */
    REPLICATE_MASTER_BEARER_TOKEN,
    
    REPLICATE_MASTER_USERNAME,
    
    REPLICATE_MASTER_PASSWORD,
    
    /**
     * User data variable specifying the "From:" address used when the server sends out e-mail notifications
     */
    MAIL_FROM,
    
    /**
     * User data variable specifying the hostname or IP address of the host to be used for SMTP connections to
     * send out e-mails.
     */
    MAIL_SMTP_HOST,
    
    /**
     * User data variable used in conjunction with {@link #MAIL_SMTP_HOST}, specifying the port number for SMTP
     * connections; usually this should be 25 for unencrypted SMTP connections.
     */
    MAIL_SMTP_PORT,
    
    /**
     * User data variable specifying either {@code true} or {@code false}, switching SMTP authentication on or off,
     * respectively. If {@code true} is provided for this variable's value, {@link #MAIL_SMTP_USER} and {@link #MAIL_SMTP_PASSWORD}
     * need to be specified, too.
     */
    MAIL_SMTP_AUTH,
    
    /**
     * User data variable specifying an SMTP user name in case {@link #MAIL_SMTP_AUTH} was set to {@code true}
     */
    MAIL_SMTP_USER,
    
    /**
     * User data variable specifying an SMTP password in case {@link #MAIL_SMTP_AUTH} was set to {@code true}
     */
    MAIL_SMTP_PASSWORD
}
