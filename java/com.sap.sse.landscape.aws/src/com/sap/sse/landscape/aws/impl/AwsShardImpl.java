package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.ShardNameDTO;
import com.sap.sse.landscape.aws.TargetGroup;

public class AwsShardImpl<ShardingKey> implements AwsShard<ShardingKey> {

    private static final long serialVersionUID = 1L;
    final private Iterable<ShardingKey> keys;
    final private TargetGroup<ShardingKey> targetgroup;
    final private String shardname;
    final private String name;
    private AwsAutoScalingGroup autoscalinggroup;
    private ShardNameDTO shardNameDTO;

    public AwsShardImpl(String shardname, Iterable<ShardingKey> keys, TargetGroup<ShardingKey> targetgroup,
            ShardNameDTO shardnameDTO) {
        this.keys = keys;
        this.targetgroup = targetgroup;
        this.shardname = shardname;
        this.setShardNameDTO(shardnameDTO);
        this.name = shardnameDTO.getName();
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

    public ShardNameDTO getShardNameDTO() {
        return shardNameDTO;
    }

    public void setShardNameDTO(ShardNameDTO shardNameDTO) {
        this.shardNameDTO = shardNameDTO;
    }

}
