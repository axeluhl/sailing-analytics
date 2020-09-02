package com.sap.sse.landscape.aws.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.aws.TargetGroup;

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
public class ApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends NamedImpl implements ReverseProxy<ShardingKey, MetricsT> {
    private static final long serialVersionUID = 8019146973512856147L;
    private final AwsLandscape<ShardingKey, MetricsT> landscape;
    private final AwsRegion region;
    private final String targetGroupArn;
    
    public ApacheReverseProxy(String name, AwsLandscape<ShardingKey, MetricsT> landscape, AwsRegion region, Map<AwsAvailabilityZone, Integer> numberOfInstancesPerAz) {
        super(name);
        this.landscape = landscape;
        this.region = region;
        final TargetGroup targetGroup = landscape.createTargetGroup(region, getTargetGroupName(), /* port */ 80,
                getHealthCheckUrl(), /* health check port */ 80);
        targetGroupArn = targetGroup.getTargetGroupArn();
        for (final Entry<AwsAvailabilityZone, Integer> e : numberOfInstancesPerAz.entrySet()) {
            targetGroup.addTargets(addHosts(getDefaultInstanceType(), e.getKey(), e.getValue()));
        }
    }

    private String getHealthCheckUrl() {
        return "/internal-server-status";
    }

    private String getTargetGroupName() {
        return "ReverseProxy-"+getName();
    }

    @Override
    public void setScopeRedirect(Scope<ShardingKey> scope,
            ApplicationReplicaSet<ShardingKey, MetricsT> applicationReplicaSet) {
        // TODO Implement ApacheReverseProxy.setScopeRedirect(...)
        
    }

    @Override
    public void setPlainRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT> applicationReplicaSet) {
        // TODO Implement ReverseProxy.setPlainRedirect(...)
        
    }

    @Override
    public void setHomeRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT> applicationReplicaSet) {
        // TODO Implement ReverseProxy.setHomeRedirect(...)
        
    }

    @Override
    public void setEventRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT> applicationReplicaSet, UUID eventId) {
        // TODO Implement ReverseProxy.setEventRedirect(...)
        
    }

    @Override
    public void setEventSeriesRedirect(String hostname,
            ApplicationReplicaSet<ShardingKey, MetricsT> applicationReplicaSet,
            UUID leaderboardGroupId) {
        // TODO Implement ReverseProxy.setEventSeriesRedirect(...)
        
    }

    @Override
    public void removeRedirect(String hostname) {
        // TODO Implement ReverseProxy.removeRedirect(...)
        
    }

    @Override
    public Iterable<AwsInstance> getHosts() {
        // TODO Implement ReverseProxy.getHosts(...)
        return null;
    }

    @Override
    public Iterable<AwsInstance> addHosts(InstanceType instanceType, AwsAvailabilityZone az, int numberOfHostsToAdd) {
        // TODO Implement ReverseProxy.addHost(...)
        return null;
    }
    
    @Override
    public void removeHost(AwsInstance host) {
        assert Util.contains(getHosts(), host);
        if (Util.size(getHosts()) == 1) {
            throw new IllegalStateException("Trying to remove the last hosts of reverse proxy "+this+". Use terminate() instead");
        }
        landscape.terminate(host);
    }
    
    @Override
    public void terminate() {
        Set<AwsInstance> hosts = new HashSet<>();
        Util.addAll(getHosts(), hosts);
        for (final AwsInstance host : hosts) {
            landscape.terminate(host);
        }
        landscape.deleteTargetGroup(getTargetGroup());
    }

    @Override
    public TargetGroup getTargetGroup() {
        return landscape.getTargetGroup(region, getTargetGroupName(), targetGroupArn);
    }
}