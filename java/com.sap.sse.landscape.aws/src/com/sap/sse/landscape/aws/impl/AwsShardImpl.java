package com.sap.sse.landscape.aws.impl;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

public class AwsShardImpl<ShardingKey> implements AwsShard<ShardingKey> {

    private static final long serialVersionUID = 1L;
    private final Iterable<ShardingKey> keys;
    private final TargetGroup<ShardingKey> targetGroup;
    private final String replicaSetName;
    private final String name;
    private final AwsAutoScalingGroup autoScalingGroup;
    private final ApplicationLoadBalancer<ShardingKey> loadBalancer;
    private final Iterable<Rule> rules;

    public AwsShardImpl(String replicaSetName, String shardName, Iterable<ShardingKey> keys,
            TargetGroup<ShardingKey> targetgroup, ApplicationLoadBalancer<ShardingKey> loadBalancer, Iterable<Rule> rules, AwsAutoScalingGroup asg) {
        this.keys = keys;
        this.targetGroup = targetgroup;
        this.replicaSetName = replicaSetName;
        this.name = shardName;
        this.loadBalancer = loadBalancer;
        this.autoScalingGroup = asg;
        this.rules = rules;
    }

    @Override
    public Iterable<ShardingKey> getKeys() {
        return keys;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReplicaSetName() {
        return replicaSetName;
    }

    @Override
    public TargetGroup<ShardingKey> getTargetGroup() {
        return targetGroup;
    }

    @Override
    public AwsAutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey> getLoadBalancer() {
        return loadBalancer;
    }

    @Override
    public Iterable<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        return "AwsShardImpl [name=" + name + ", replicaSetName=" + replicaSetName + ", keys=" + Util.joinStrings(", ", keys) + "]";
    }
    
}
