package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxyCluster;
import com.sap.sse.landscape.aws.Tags;

import software.amazon.awssdk.services.ec2.model.InstanceType;

public class ApacheReverseProxyCluster<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
LogT extends Log>
        extends AbstractApacheReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>
implements ReverseProxyCluster<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, RotatingFileBasedLog> {
    private Set<AwsInstance<ShardingKey, MetricsT>> hosts;

    public ApacheReverseProxyCluster(AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super(landscape);
        this.hosts = new HashSet<>();
    }
    
    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getHosts() {
        return Collections.unmodifiableCollection(hosts);
    }
    
    private Iterable<ApacheReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>> getReverseProxies() {
        return Util.map(hosts, host->new ApacheReverseProxy<>(getLandscape(), host));
    }

    @Override
    public void addHost(AwsInstance<ShardingKey, MetricsT> host) {
        hosts.add(host);
    }
    
    @Override
    public AwsInstance<ShardingKey, MetricsT> createHost(InstanceType instanceType, AwsAvailabilityZone az, String keyName) {
        return getLandscape().launchHost((instanceId, availabilityZone, landscape)->new AwsInstanceImpl<ShardingKey, MetricsT>(instanceId, availabilityZone, landscape),
                getAmiId(), instanceType, az, keyName, Collections.singleton(getSecurityGroup(az.getRegion())), Optional.of(Tags.with("Name", "ReverseProxy")));
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
    public void setPlainRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        getReverseProxies().forEach(proxy->proxy.setPlainRedirect(hostname, applicationReplicaSet));
    }

    @Override
    public void setHomeRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        getReverseProxies().forEach(proxy->proxy.setHomeRedirect(hostname, applicationReplicaSet));
    }

    @Override
    public void setEventRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet,
            UUID eventId) {
        getReverseProxies().forEach(proxy->proxy.setEventRedirect(hostname, applicationReplicaSet, eventId));
    }

    @Override
    public void setEventSeriesRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet,
            UUID leaderboardGroupId) {
        getReverseProxies().forEach(proxy->proxy.setEventSeriesRedirect(hostname, applicationReplicaSet, leaderboardGroupId));
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        getReverseProxies().forEach(proxy->proxy.setScopeRedirect(scope, applicationReplicaSet));
    }

    @Override
    public void removeRedirect(String hostname) {
        getReverseProxies().forEach(proxy->proxy.removeRedirect(hostname));
    }
}
