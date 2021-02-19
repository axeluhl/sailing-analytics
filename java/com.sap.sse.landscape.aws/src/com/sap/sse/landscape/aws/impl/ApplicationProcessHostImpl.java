package com.sap.sse.landscape.aws.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.ssh.SshCommandChannel;

public class ApplicationProcessHostImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
extends AwsInstanceImpl<ShardingKey>
implements ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> {
    private static final Logger logger = Logger.getLogger(ApplicationProcessHostImpl.class.getName());
    private final ProcessFactory<ShardingKey, MetricsT, ProcessT> processFactoryFromHostAndServerDirectory;
    
    @FunctionalInterface
    public static interface ProcessFactory<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>> {
        ProcessT createProcess(ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> host, int port, String serverDirectory);
    }

    public ApplicationProcessHostImpl(String instanceId, AwsAvailabilityZone availabilityZone,
            AwsLandscape<ShardingKey> landscape, ProcessFactory<ShardingKey, MetricsT, ProcessT> processFactoryFromHostAndServerDirectory) {
        super(instanceId, availabilityZone, landscape);
        this.processFactoryFromHostAndServerDirectory = processFactoryFromHostAndServerDirectory;
    }
    
    @Override
    public AwsLandscape<ShardingKey> getLandscape() {
        return super.getLandscape();
    }

    @Override
    public ReverseProxy<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> getReverseProxy() {
        return new ApacheReverseProxy<>(getLandscape(), this);
    }

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
     */
    @Override
    public Iterable<ProcessT> getApplicationProcesses(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        final String SERVER_DIRECTORY_JSON_PROPERTY = "serverdirectory";
        final String SERVER_PORT_JSON_PROEPRTY = "port";
        final Set<ProcessT> result = new HashSet<>();
        final SshCommandChannel sshChannel = createRootSshChannel(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
        final String stdout = sshChannel.runCommandAndReturnStdoutAndLogStderr(
                "cd "+DEFAULT_SERVERS_PATH+"; "+
                "echo -n \"[\"; "+
                "find * -type d -prune -exec bash -c 'cd '{}'; . env.sh >/dev/null; echo \"{ \\\""+
                        SERVER_DIRECTORY_JSON_PROPERTY+"\\\":\\\"'{}'\\\", \\\""+
                        SERVER_PORT_JSON_PROEPRTY+"\\\":${SERVER_PORT} },\"' \\; ; echo \"{}]\"",
                "Error(s) during evaluating server processes", Level.SEVERE);
        final JSONArray serverDirectoriesAndPorts = (JSONArray) new JSONParser().parse(stdout);
        for (final Object serverDirectoryAndPort : serverDirectoriesAndPorts) {
            final JSONObject serverDirectoryAndPortObject = (JSONObject) serverDirectoryAndPort;
            ProcessT process;
            final String relativeServerDirectory = (String) serverDirectoryAndPortObject.get(SERVER_DIRECTORY_JSON_PROPERTY);
            if (relativeServerDirectory != null) { // null means we got the empty "terminator" record
                final String serverDirectory = DEFAULT_SERVERS_PATH+"/"+relativeServerDirectory;
                final int port = ((Number) serverDirectoryAndPortObject.get(SERVER_PORT_JSON_PROEPRTY)).intValue();
                try {
                    process = processFactoryFromHostAndServerDirectory.createProcess(this, port, serverDirectory);
                    result.add(process);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Problem creating application process from directory "+serverDirectory+" on host "+this+"; skipping", e);
                }
            }
        }
        sshChannel.disconnect();
        return result;
    }
}
