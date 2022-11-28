package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.aws.AwsAutoScalingGroup;
import com.sap.sse.landscape.aws.AwsShard;
import com.sap.sse.landscape.aws.TargetGroup;

public class AwsShardImpl<ShardingKey> implements AwsShard<ShardingKey> {
    
    private static final long serialVersionUID = 1L;
    final private Iterable<ShardingKey> keys;
    final private TargetGroup<ShardingKey> targetgroup;
    final private String shardname;
    final private String name;
    private AwsAutoScalingGroup autoscalinggroup;
    public AwsShardImpl(String name,String shardname,Iterable<ShardingKey> keys, TargetGroup<ShardingKey> targetgroup){
        this.keys = keys;
        this.targetgroup  =targetgroup;
        this.name = name;
        this.shardname  =shardname;
    }
    
    public void setAutoscalingGroup(AwsAutoScalingGroup asg) {
        if(autoscalinggroup == null) {
            autoscalinggroup  =asg;
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

}
