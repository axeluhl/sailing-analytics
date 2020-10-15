package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
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
        extends ApacheReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, LogT>
implements ReverseProxyCluster<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, LogT> {
    private static final long serialVersionUID = 7193925434183030817L;
    private Set<AwsInstance<ShardingKey, MetricsT>> hosts;
    private final AwsRegion region;

    public ApacheReverseProxyCluster(String name,
            AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, AwsRegion region) {
        super(name, landscape, region);
        this.hosts = new HashSet<>();
        this.region = region;
    }
    
    @Override
    public Iterable<AwsInstance<ShardingKey, MetricsT>> getHosts() {
        return Collections.unmodifiableCollection(hosts);
    }

    @Override
    public void addHost(AwsInstance<ShardingKey, MetricsT> host) {
        hosts.add(host);
    }
    
    @Override
    public AwsInstance<ShardingKey, MetricsT> createHost(InstanceType instanceType, AwsAvailabilityZone az, String keyName) {
        return getLandscape().launchHost(getAmiId(), instanceType, az, keyName, Collections.singleton(getSecurityGroup()), Optional.of(Tags.with("Name", "ReverseProxy-"+getName())));
    }
    
    @Override
    public void removeHost(AwsInstance<ShardingKey, MetricsT> host) {
        assert Util.contains(getHosts(), host);
        if (Util.size(getHosts()) == 1) {
            throw new IllegalStateException("Trying to remove the last hosts of reverse proxy "+this+". Use terminate() instead");
        }
        getLandscape().terminate(host); // this assumes that the host is running only the reverse proxy process...
    }

    private SecurityGroup getSecurityGroup() {
        return getLandscape().getDefaultSecurityGroupForCentralReverseProxy(region);
    }

    private MachineImage<AwsInstance<ShardingKey, MetricsT>> getAmiId() {
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
}
