package com.sap.sse.landscape.aws.impl;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ProcessFactory;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.ssh.SshCommandChannel;

public class ApplicationProcessHostImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>,
HostT extends ApplicationProcessHost<ShardingKey, MetricsT, ProcessT>>
extends AwsInstanceImpl<ShardingKey>
implements ApplicationProcessHost<ShardingKey, MetricsT, ProcessT> {
    private static final Logger logger = Logger.getLogger(ApplicationProcessHostImpl.class.getName());
    private final ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactoryFromHostAndServerDirectory;
    
    public ApplicationProcessHostImpl(String instanceId, AwsAvailabilityZone availabilityZone,
            InetAddress privateIpAddress, TimePoint launchTimePoint,
            AwsLandscape<ShardingKey> landscape, ProcessFactory<ShardingKey, MetricsT, ProcessT, HostT> processFactoryFromHostAndServerDirectory) {
        super(instanceId, availabilityZone, privateIpAddress, launchTimePoint, landscape);
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
     * Used by {@link #getApplicationProcesses(Optional, Optional, byte[])} to extract further properties from the
     * process's set of environment variables, putting them into the resulting JSON document when querying the
     * environment once (and for all...) and from there into the {@link ProcessFactory} when creating each process
     * instance. This default implementation returns an empty map.
     * 
     * @return a map (may be immutable) whose keys are the environment variable names to query; e.g.,
     *         {@code "SERVER_PORT"} if that wasn't already part of the default set of variables; the names are used in
     *         their {@link String#toLowerCase()} form for representing the variable in the JSON document produced by
     *         the SSH command that lists them. The values are {@code true} if the variable's type is a {@link String}
     *         and therefore needs corresponding quoting in the JSON document.
     */
    protected Map<String, Boolean> getAdditionalEnvironmentPropertiesAndWhetherStringTyped() {
        return Collections.emptyMap();
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
        final String SERVER_PORT_JSON_PROPERTY = "port";
        final String TELNET_PORT_JSON_PROPERTY = "telnetport";
        final String SERVER_NAME_JSON_PROPERTY = "servername";
        final Set<ProcessT> result = new HashSet<>();
        final SshCommandChannel sshChannel = createRootSshChannel(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
        if (sshChannel != null) { // could, e.g., have timed out
            try {
                final StringBuilder commandLine = new StringBuilder("cd "+DEFAULT_SERVERS_PATH+"; ")
                    .append("echo -n \"[\"; ")
                    .append("find * -type d -prune -exec bash -c 'cd '{}'; . env.sh >/dev/null; echo \"{ ")
                        .append("\\\""+SERVER_DIRECTORY_JSON_PROPERTY+"\\\":\\\"'{}'\\\", ")
                        .append("\\\""+SERVER_PORT_JSON_PROPERTY+"\\\":${SERVER_PORT} ")
                        .append("\\\""+TELNET_PORT_JSON_PROPERTY+"\\\":${TELNET_PORT}, ")
                        .append("\\\""+SERVER_NAME_JSON_PROPERTY+"\\\":\\\"${SERVER_NAME}\\\", ");
                // query the additional environment properties as specified by getAdditionalEnvironmentPropertiesAndWhetherStringTyped()
                for (final Entry<String, Boolean> additionalEntryAndWhetherStringTyped : getAdditionalEnvironmentPropertiesAndWhetherStringTyped().entrySet()) {
                    commandLine.append("\\\""+additionalEntryAndWhetherStringTyped.getKey().toLowerCase()+"\\\":");
                    if (additionalEntryAndWhetherStringTyped.getValue()) {
                        commandLine.append("\\\"");
                    }
                    commandLine.append("${");
                    commandLine.append(additionalEntryAndWhetherStringTyped.getKey());
                    commandLine.append("}");
                    if (additionalEntryAndWhetherStringTyped.getValue()) {
                        commandLine.append("\\\"");
                    }
                    commandLine.append(", ");
                }
                commandLine.append("},\"' \\; ; echo \"{}]\"");
                final String stdout = sshChannel.runCommandAndReturnStdoutAndLogStderr(
                        commandLine.toString(),
                        "Error(s) during evaluating server processes", Level.SEVERE);
                final JSONArray serverDirectoriesAndPorts = (JSONArray) new JSONParser().parse(stdout);
                for (final Object serverDirectoryAndPort : serverDirectoriesAndPorts) {
                    final JSONObject serverDirectoryAndPortObject = (JSONObject) serverDirectoryAndPort;
                    ProcessT process;
                    final String relativeServerDirectory = (String) serverDirectoryAndPortObject.get(SERVER_DIRECTORY_JSON_PROPERTY);
                    if (relativeServerDirectory != null) { // null means we got the empty "terminator" record
                        final String serverDirectory = DEFAULT_SERVERS_PATH+"/"+relativeServerDirectory;
                        final int port = ((Number) serverDirectoryAndPortObject.get(SERVER_PORT_JSON_PROPERTY)).intValue();
                        final int telnetPort = ((Number) serverDirectoryAndPortObject.get(TELNET_PORT_JSON_PROPERTY)).intValue();
                        final String serverName = (String) serverDirectoryAndPortObject.get(SERVER_NAME_JSON_PROPERTY);
                        final Map<String, Object> additionalProperties = new HashMap<>();
                        for (final Entry<String, Boolean> additionalEntryAndWhetherStringTyped : getAdditionalEnvironmentPropertiesAndWhetherStringTyped().entrySet()) {
                            additionalProperties.put(additionalEntryAndWhetherStringTyped.getKey(), serverDirectoryAndPortObject.get(additionalEntryAndWhetherStringTyped.getKey().toLowerCase()));
                        }
                        try {
                            process = processFactoryFromHostAndServerDirectory.createProcess(self(), port, serverDirectory, telnetPort, serverName, additionalProperties);
                            result.add(process);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Problem creating application process from directory "+serverDirectory+" on host "+this+"; skipping", e);
                        }
                    }
                }
            } finally {
                sshChannel.disconnect();
            }
        }
        return result;
    }

    private HostT self() {
        @SuppressWarnings("unchecked")
        final HostT result = (HostT) this;
        return result;
    }
}
