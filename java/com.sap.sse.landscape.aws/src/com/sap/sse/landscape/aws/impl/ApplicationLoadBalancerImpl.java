package com.sap.sse.landscape.aws.impl;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ProtocolEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

public class ApplicationLoadBalancerImpl<ShardingKey, MetricsT extends ApplicationProcessMetrics>
implements ApplicationLoadBalancer<ShardingKey, MetricsT> {
    private static final long serialVersionUID = -5297220031399131769L;
    
    private final LoadBalancer loadBalancer;

    private final Region region;

    private final AwsLandscape<ShardingKey, MetricsT, ?, ?> landscape;
    
    public ApplicationLoadBalancerImpl(Region region, LoadBalancer loadBalancer, AwsLandscape<ShardingKey, MetricsT, ?, ?> landscape) {
        this.region = region;
        this.loadBalancer = loadBalancer;
        this.landscape = landscape;
    }

    @Override
    public String getName() {
        return loadBalancer.loadBalancerName();
    }

    @Override
    public String getDNSName() {
        return loadBalancer.dnsName();
    }

    @Override
    public String getArn() {
        return loadBalancer.loadBalancerArn();
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public Iterable<Rule> getRules() {
        final Listener httpsListener = getListener(ProtocolEnum.HTTPS);
        return landscape.getLoadBalancerListenerRules(httpsListener, getRegion());
    }
    
    @Override
    public void deleteListener(Listener listener) {
        landscape.deleteLoadBalancerListener(getRegion(), listener);
    }

    private Listener getListener(ProtocolEnum protocol) {
        return Util.filter(landscape.getListeners(this), l->l.protocol() == protocol).iterator().next();
    }

    @Override
    public Iterable<Rule> addRules(Rule... rulesToAdd) {
        return landscape.createLoadBalancerListenerRules(region, getListener(ProtocolEnum.HTTPS), rulesToAdd);
    }

    @Override
    public void deleteRules(Rule... rulesToDelete) {
        landscape.deleteLoadBalancerListenerRules(region, rulesToDelete);
    }

    @Override
    public Iterable<TargetGroup<ShardingKey, MetricsT>> getTargetGroups() {
        return landscape.getTargetGroupsByLoadBalancerArn(getRegion(), getArn());
    }
    
    private void deleteAllRules() {
        for (final Rule rule : getRules()) {
            if (!rule.isDefault()) {
                deleteRules(rule);
            }
        }
    }

    @Override
    public void delete() throws InterruptedException {
        // first obtain the target groups to which, based on the current ALB configuration, traffic is forwarded
        final Iterable<TargetGroup<ShardingKey, MetricsT>> targetGroups = getTargetGroups();
        // now delete the rules to free up all target groups to which the ALB could have forwarded, except the default rule
        deleteAllRules();
        deleteAllListeners();
        landscape.deleteLoadBalancer(this);
        Thread.sleep(Duration.ONE_SECOND.times(5).asMillis()); // wait a bit until the target groups are no longer considered "in use"
        // now that all target groups the ALB used are freed up, delete them:
        for (final TargetGroup<?, ?> targetGroup : targetGroups) {
            landscape.deleteTargetGroup(landscape.getTargetGroup(getRegion(), targetGroup.getName(), targetGroup.getTargetGroupArn()));
        }
    }
    
    private void deleteListener(ProtocolEnum protocol) {
        final Listener httpListener = getListener(protocol);
        if (httpListener != null) {
            deleteListener(httpListener);
        }
    }
    
    private void deleteAllListeners() {
        deleteListener(ProtocolEnum.HTTP);
        deleteListener(ProtocolEnum.HTTPS);
    }
}
