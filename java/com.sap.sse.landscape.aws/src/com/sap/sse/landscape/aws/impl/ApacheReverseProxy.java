package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.SecurityGroup;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.aws.Tags;

import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * An Apache2-based reverse proxy implementation (httpd) that makes specific assumptions about the availability of an
 * {@link AmazonMachineImage} that can be used to launch and configure such a reverse proxy instance on one or more
 * instances running in one or more {@link AwsAvailabilityZone availability zones}.<p>
 * 
 * TODO how do we remember the hosts/instances/nodes/processes that together form this {@link ApacheReverseProxy}? DB Persistence? Tags?
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
extends NamedImpl implements ReverseProxy<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private static final String DEFAULT_SECURITY_GROUP_ID = "sg-0b2afd48960251280";
    private static final long serialVersionUID = 8019146973512856147L;
    private final AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
    private final AwsRegion region;
    private Set<AwsInstance<ShardingKey, MetricsT>> hosts;
    
    public ApacheReverseProxy(String name, AwsLandscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape, AwsRegion region) {
        super(name);
        this.landscape = landscape;
        this.region = region;
        this.hosts = new HashSet<>();
    }

    @Override
    public String getHealthCheckPath() {
        return "/internal-server-status";
    }

    @Override
    public String getTargetGroupName() {
        return "ReverseProxy-"+getName();
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        // TODO Implement ApacheReverseProxy.setScopeRedirect(...)
        
    }

    @Override
    public void setPlainRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        // TODO Implement ReverseProxy.setPlainRedirect(...)
        
    }

    @Override
    public void setHomeRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet) {
        // TODO Implement ReverseProxy.setHomeRedirect(...)
        
    }

    @Override
    public void setEventRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet, UUID eventId) {
        // TODO Implement ReverseProxy.setEventRedirect(...)
        
    }

    @Override
    public void setEventSeriesRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> applicationReplicaSet,
            UUID leaderboardGroupId) {
        // TODO Implement ReverseProxy.setEventSeriesRedirect(...)
        
    }

    @Override
    public void removeRedirect(String hostname) {
        // TODO Implement ReverseProxy.removeRedirect(...)
        
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
        return landscape.launchHost(getAmiId(), instanceType, az, keyName, Collections.singleton(getSecurityGroup()), Optional.of(Tags.with("Name", "ReverseProxy-"+getName())));
    }
    
    private SecurityGroup getSecurityGroup() {
        // TODO make this dynamic, based, e.g., on tags
        return landscape.getSecurityGroup(DEFAULT_SECURITY_GROUP_ID, region);
    }

    private MachineImage<AwsInstance<ShardingKey, MetricsT>> getAmiId() {
        // TODO Implement ApacheReverseProxy.getAmiId(...)
        return null;
    }

    @Override
    public void removeHost(AwsInstance<ShardingKey, MetricsT> host) {
        assert Util.contains(getHosts(), host);
        if (Util.size(getHosts()) == 1) {
            throw new IllegalStateException("Trying to remove the last hosts of reverse proxy "+this+". Use terminate() instead");
        }
        landscape.terminate(host); // this assumes that the host is running only the reverse proxy process...
    }
    
    @Override
    public void terminate() {
        Set<AwsInstance<ShardingKey, MetricsT>> hosts = new HashSet<>();
        Util.addAll(getHosts(), hosts);
        for (final AwsInstance<ShardingKey, MetricsT> host : hosts) {
            landscape.terminate(host);
        }
    }
}