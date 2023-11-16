package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.concurrent.ConsumerWithException;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxyCluster;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.TargetGroup;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;
import com.sap.sse.shared.util.Wait;

import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InstanceType;

public class ApacheReverseProxyCluster<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, LogT extends Log>
extends AbstractApacheReverseProxy<ShardingKey, MetricsT, ProcessT>
implements ReverseProxyCluster<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> {
    private Set<AwsInstance<ShardingKey>> hosts;
    private static final Logger logger = Logger.getLogger(ApacheReverseProxyCluster.class.getName());
    public ApacheReverseProxyCluster(AwsLandscape<ShardingKey> landscape) {
        super(landscape);
        this.hosts = new HashSet<>();
    }
    
    @Override
    public Iterable<AwsInstance<ShardingKey>> getHosts() {
        return Collections.unmodifiableCollection(hosts);
    }
    
    private Iterable<ApacheReverseProxy<ShardingKey, MetricsT, ProcessT>> getReverseProxies() {
        return Util.map(hosts, host->new ApacheReverseProxy<>(getLandscape(), host));
    }

    @Override
    public void addHost(AwsInstance<ShardingKey> host) {
        hosts.add(host);
    }
    
    @Override
    public AwsInstance<ShardingKey> createHost(String name, InstanceType instanceType, AwsAvailabilityZone az, String keyName) throws TimeoutException, Exception {
        final AwsInstance<ShardingKey> host = getLandscape().launchHost(
                (instanceId, availabilityZone, privateIpAddress, launchTimePoint, landscape) -> new AwsInstanceImpl<ShardingKey>(instanceId,
                        availabilityZone, privateIpAddress, launchTimePoint, landscape),
                getAmiId(az.getRegion()), instanceType, az, keyName,
                getSecurityGroups(az.getRegion()), Optional.of(Tags.with(StartAwsHost.NAME_TAG_NAME, name).and(SharedLandscapeConstants.DISPOSABLE_PROXY, "").and(SharedLandscapeConstants.REVERSE_PROXY_TAG_NAME, "")));
        addHost(host);
        Wait.wait(() -> !host.getInstance().state().name().equals(InstanceStateName.PENDING), Optional.of(Duration.ofSeconds(360)), Duration.ONE_MINUTE, Level.WARNING, RETRY_ADD_TO_TARGET_GROUP );
        for (TargetGroup<ShardingKey> targetGroup : getLandscape().getTargetGroups(az.getRegion())) {
            targetGroup.getTagDescriptions().forEach(description -> description.tags().forEach(tag -> {
                if (tag.key().equals(SharedLandscapeConstants.ALL_REVERSE_PROXIES)) {
                    targetGroup.addTarget(host);
                }
            }));
        }
        return host;
    }
    
    @Override
    public void removeHost(AwsInstance<ShardingKey> host) {
        assert Util.contains(getHosts(), host);
        if (Util.size(getHosts()) == 1) {
            throw new IllegalStateException(
                    "Trying to remove the last hosts of reverse proxy " + this + ". Use terminate() instead");
        }
        getLandscape().terminate(host); // this assumes that the host is running only the reverse proxy process...
    }

    /**
     * Gets the security groups in the region that a reverse proxy instance should or does have.
 
     */
    private List<SecurityGroup> getSecurityGroups(Region region) {
        return getLandscape().getDefaultSecurityGroupsForCentralReverseProxy(region); 
        
    }

    /**
     * Gets the latest image in the current region with the correct tag for creating a reverse proxy.
     * @param region 
     * @return
     */
    private MachineImage getAmiId(Region region) {
        return getLandscape().getLatestImageWithType(region, SharedLandscapeConstants.IMAGE_TYPE_REVERSE_PROXY);
    }

    @Override
    public void terminate() {
        Set<AwsInstance<ShardingKey>> hosts = new HashSet<>();
        Util.addAll(getHosts(), hosts);
        for (final AwsInstance<ShardingKey> host : hosts) {
            getLandscape().terminate(host);
        }
    }

    private void setRedirect(ConsumerWithException<ApacheReverseProxy<ShardingKey, MetricsT, ProcessT>> redirectSetter)
            throws Exception {
        if (getReverseProxies().iterator().hasNext()) {
            final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy = getReverseProxies().iterator().next();
            redirectSetter.accept(proxy);
        }
    }
    
    @Override
    public void setPlainRedirect(String hostname, ProcessT applicationProcess, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setPlainRedirect(hostname, applicationProcess, optionalKeyName,
                privateKeyEncryptionPassphrase));
    }

    @Override
    public void setHomeRedirect(String hostname, ProcessT applicationProcess, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setHomeRedirect(hostname, applicationProcess, optionalKeyName,
                privateKeyEncryptionPassphrase));
    }

    @Override
    public void setEventRedirect(String hostname, ProcessT applicationProcess,
            UUID eventId, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setEventRedirect(hostname, applicationProcess, eventId, optionalKeyName,
                privateKeyEncryptionPassphrase));
    }

    @Override
    public void setEventSeriesRedirect(String hostname, ProcessT applicationProcess, UUID leaderboardGroupId,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setEventSeriesRedirect(hostname, applicationProcess, leaderboardGroupId,
                optionalKeyName, privateKeyEncryptionPassphrase));
    }

    @Override
    public void setEventArchiveRedirect(String hostname, UUID eventId, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setEventArchiveRedirect(hostname, eventId, optionalKeyName,
                privateKeyEncryptionPassphrase));
    }

    @Override
    public void setEventSeriesArchiveRedirect(String hostname, UUID leaderboardGroupId,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setEventSeriesArchiveRedirect(hostname, leaderboardGroupId, optionalKeyName,
                privateKeyEncryptionPassphrase));
    }

    @Override
    public void setHomeArchiveRedirect(String hostname, Optional<String> optionalKeyName,
            byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.setHomeArchiveRedirect(hostname, optionalKeyName, privateKeyEncryptionPassphrase));
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope, ProcessT applicationProcess) {
        try {
        setRedirect(proxy -> proxy.setScopeRedirect(scope, applicationProcess));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.toString());
        }
    }

    @Override
    public void createInternalStatusRedirect(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.createInternalStatusRedirect(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase));
    }

    @Override
    public void removeRedirect(String hostname, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.removeRedirect(hostname, optionalKeyName, privateKeyEncryptionPassphrase));
    }

    @Override
    public void removeRedirect(Scope<ShardingKey> scope, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        setRedirect(proxy -> proxy.removeRedirect(scope, optionalKeyName, privateKeyEncryptionPassphrase));
    }
}
