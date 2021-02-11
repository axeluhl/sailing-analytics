package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
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

import software.amazon.awssdk.services.ec2.model.InstanceType;

public class ApacheReverseProxyCluster<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, LogT extends Log>
extends AbstractApacheReverseProxy<ShardingKey, MetricsT, ProcessT>
implements ReverseProxyCluster<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> {
    private Set<AwsInstance<ShardingKey, MetricsT>> hosts;

    public ApacheReverseProxyCluster(AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape) {
        super(landscape);
        this.hosts = new HashSet<>();
    }
    
    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getHosts() {
        return Collections.unmodifiableCollection(hosts);
    }
    
    private Iterable<ApacheReverseProxy<ShardingKey, MetricsT, ProcessT>> getReverseProxies() {
        return Util.map(hosts, host->new ApacheReverseProxy<>(getLandscape(), host));
    }

    @Override
    public void addHost(AwsInstance<ShardingKey, MetricsT> host) {
        hosts.add(host);
    }
    
    @Override
    public AwsInstance<ShardingKey, MetricsT> createHost(InstanceType instanceType, AwsAvailabilityZone az, String keyName) {
        final AwsInstance<ShardingKey, MetricsT> host = getLandscape().launchHost(
                (instanceId, availabilityZone, landscape) -> new AwsInstanceImpl<ShardingKey, MetricsT>(instanceId,
                        availabilityZone, landscape),
                getAmiId(), instanceType, az, keyName,
                Collections.singleton(getSecurityGroup(az.getRegion())), Optional.of(Tags.with("Name", "ReverseProxy")));
        addHost(host);
        return host;
    }
    
    @Override
    public void removeHost(AwsInstance<ShardingKey, MetricsT> host) {
        assert Util.contains(getHosts(), host);
        if (Util.size(getHosts()) == 1) {
            throw new IllegalStateException("Trying to remove the last hosts of reverse proxy "+this+". Use terminate() instead");
        }
        getLandscape().terminate(host); // this assumes that the host is running only the reverse proxy process...
    }

    private SecurityGroup getSecurityGroup(Region region) {
        return getLandscape().getDefaultSecurityGroupForCentralReverseProxy(region);
    }

    private MachineImage getAmiId() {
        // TODO Implement ApacheReverseProxy.getAmiId(...)
        return null;
    }

    @Override
    public void terminate() {
        Set<AwsInstance<ShardingKey, MetricsT>> hosts = new HashSet<>();
        Util.addAll(getHosts(), hosts);
        for (final AwsInstance<ShardingKey, MetricsT> host : hosts) {
            getLandscape().terminate(host);
        }
    }

    @Override
    public void setPlainRedirect(String hostname, ProcessT applicationProcess, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.setPlainRedirect(hostname, applicationProcess, optionalKeyName, privateKeyEncryptionPassphrase);
        }
    }

    @Override
    public void setHomeRedirect(String hostname, ProcessT applicationProcess, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.setHomeRedirect(hostname, applicationProcess, optionalKeyName, privateKeyEncryptionPassphrase);
        }
    }

    @Override
    public void setEventRedirect(String hostname, ProcessT applicationProcess,
            UUID eventId, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.setEventRedirect(hostname, applicationProcess, eventId, optionalKeyName, privateKeyEncryptionPassphrase);
        }
    }

    @Override
    public void setEventSeriesRedirect(String hostname, ProcessT applicationProcess,
            UUID leaderboardGroupId, byte[] privateKeyEncryptionPassphrase, Optional<String> optionalKeyName) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.setEventSeriesRedirect(hostname, applicationProcess, leaderboardGroupId, privateKeyEncryptionPassphrase, optionalKeyName);
        }
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope, ProcessT applicationProcess) {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.setScopeRedirect(scope, applicationProcess);
        }
    }

    @Override
    public void createInternalStatusRedirect(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.createInternalStatusRedirect(optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
        }
    }

    @Override
    public void removeRedirect(String hostname, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.removeRedirect(hostname, optionalKeyName, privateKeyEncryptionPassphrase);
        }
    }

    @Override
    public void removeRedirect(Scope<ShardingKey> scope, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        for (final ApacheReverseProxy<ShardingKey, MetricsT, ProcessT> proxy : getReverseProxies()) {
            proxy.removeRedirect(scope, optionalKeyName, privateKeyEncryptionPassphrase);
        }
    }
}
