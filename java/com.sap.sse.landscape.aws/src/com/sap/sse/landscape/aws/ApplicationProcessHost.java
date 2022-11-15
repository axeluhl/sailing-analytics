package com.sap.sse.landscape.aws;

import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.Scope;

public interface ApplicationProcessHost<ShardingKey,
MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AwsInstance<ShardingKey> {
    String DEFAULT_SERVERS_PATH = "/home/sailing/servers";
    
    String DEFAULT_SERVER_DIRECTORY_NAME = "server";
    
    String DEFAULT_SERVER_PATH = DEFAULT_SERVERS_PATH+"/"+DEFAULT_SERVER_DIRECTORY_NAME;
    
    /**
     * Obtains an object through which an Apache reverse proxy running on this sailing analytics host can be configured.
     * It is mainly used to decide how to route based on a URL's hostname or other {@link Scope} identification, and to
     * expand base URLs to, e.g., the URL of a specific event in that scope or an overview page of an event series or
     * simply the home/landing page. It furthermore handles logging in a consistent way.
     */
    ReverseProxy<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> getReverseProxy();
    
    /**
     * Obtains the Sailing Analytics processes running on this host. Can be zero or more. Should exceptions occur while trying to
     * connect to a process, the exception will be logged and that process will be skipped and ignored. Do not use this
     * if you want to be sure there is no meaningful process running on this host anymore; connectivity or authentication
     * problems may have been the reason this method couldn't talk to the process instance!
     * 
     * @param optionalKeyName
     *            the name of the SSH key pair to use to log on; must identify a key pair available for the
     *            {@link #getRegion() region} of this instance. If not provided, the the SSH private key for the key
     *            pair that was originally used when the instance was launched will be used.
     */
    Iterable<ProcessT> getApplicationProcesses(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception;

    /**
     * The implementation scans the {@link ApplicationProcessHost#DEFAULT_SERVERS_PATH application server deployment
     * folder} for sub-folders. In those sub-folders, the configuration file is analyzed for the port number to
     * instantiate an {@link ApplicationProcess} object for each one.
     * 
     * @param optionalKeyName
     *            the name of the SSH key pair to use to log on; must identify a key pair available for the
     *            {@link #getRegion() region} of this instance. If not provided, the the SSH private key for the key
     *            pair that was originally used when the instance was launched will be used.
     * @param privateKeyEncryptionPassphrase
     *            the pass phrase for the private key that belongs to the instance's public key used for start-up
     * @param rethrowExceptions
     *            whether an occurring exception should be rethrown. Pass {@code true} to receive exceptions
     *            that occurred while trying to find out about processes on hosts. Pass {@code false} to ignore
     *            such exceptions, log them and skip the process.
     */
    Iterable<ProcessT> getApplicationProcesses(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase, boolean rethrowExceptions) throws Exception;
}
