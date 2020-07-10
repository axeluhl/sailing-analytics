package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.LoadBalancerRule;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

public class ApplicationLoadBalancerImpl implements ApplicationLoadBalancer {
    private static final long serialVersionUID = -5297220031399131769L;
    
    final LoadBalancer loadBalancer;
    
    public ApplicationLoadBalancerImpl(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
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
    public Iterable<LoadBalancerRule> getRules() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRules(Iterable<LoadBalancerRule> rulesToAdd) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRules(Iterable<LoadBalancerRule> rulesToDelete) {
        // TODO Auto-generated method stub

    }

}
