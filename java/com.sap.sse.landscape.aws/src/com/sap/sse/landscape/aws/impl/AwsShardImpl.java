package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.ShardName;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

public class AwsShardImpl<ShardingKey> implements AwsShard<ShardingKey> {

    private static final long serialVersionUID = 1L;
    private final Iterable<ShardingKey> keys;
    private final TargetGroup<ShardingKey> targetGroup;
    private final String replicaSetName;
    private final String name;
    private final AwsAutoScalingGroup autoScalingGroup;
    private final ShardName shardNameDTO;
    private final ApplicationLoadBalancer<ShardingKey> loadBalancer;
    private final Iterable<Rule> rules;

    public AwsShardImpl(String replicaSetName, Iterable<ShardingKey> keys, TargetGroup<ShardingKey> targetgroup,
            ShardName shardNameDTO, ApplicationLoadBalancer<ShardingKey> loadBalancer, Iterable<Rule> rules,
            AwsAutoScalingGroup asg) {
        this.keys = keys;
        this.targetGroup = targetgroup;
        this.replicaSetName = replicaSetName;
        this.shardNameDTO = shardNameDTO;
        this.name = shardNameDTO.getName();
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

    public ShardName getShardNameDTO() {
        return shardNameDTO;
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey> getLoadBalancer() {
        return loadBalancer;
    }

    @Override
    public Iterable<Rule> getRules() {
        return rules;
    }
}
