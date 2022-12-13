package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.aws.ApplicationLoadBalancer;
import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.ShardName;
import com.sap.sse.landscape.aws.TargetGroup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;

public class AwsShardImpl<ShardingKey> implements AwsShard<ShardingKey> {

    private static final long serialVersionUID = 1L;
    final private Iterable<ShardingKey> keys;
    final private TargetGroup<ShardingKey> targetgroup;
    final private String shardname;
    final private String name;
    private AwsAutoScalingGroup autoscalinggroup;
    private ShardName shardNameDTO;
    private ApplicationLoadBalancer<ShardingKey> loadbalancer;
    private Iterable<Rule> rules;

    public AwsShardImpl(String shardname, Iterable<ShardingKey> keys, TargetGroup<ShardingKey> targetgroup,
            ShardName shardnameDTO, ApplicationLoadBalancer<ShardingKey> loadbalancer, Iterable<Rule> rules) {
        this.keys = keys;
        this.targetgroup = targetgroup;
        this.shardname = shardname;
        this.setShardNameDTO(shardnameDTO);
        this.name = shardnameDTO.getName();
        this.loadbalancer = loadbalancer;
        this.rules  =rules;
    }

    public void setAutoscalingGroup(AwsAutoScalingGroup asg) {
        if (autoscalinggroup == null) {
            autoscalinggroup = asg;
        }
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
    public String getShardname() {
        return shardname;
    }

    @Override
    public TargetGroup<ShardingKey> getTargetGroup() {
        return targetgroup;
    }

    @Override
    public AwsAutoScalingGroup getAutoScalingGroup() {
        return autoscalinggroup;
    }

    public ShardName getShardNameDTO() {
        return shardNameDTO;
    }

    public void setShardNameDTO(ShardName shardNameDTO) {
        this.shardNameDTO = shardNameDTO;
    }

    @Override
    public ApplicationLoadBalancer<ShardingKey> getLoadbalancer() {
        return loadbalancer;
    }

    @Override
    public Iterable<Rule> getRules() {
        return rules;
    }

}
