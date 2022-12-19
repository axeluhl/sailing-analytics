package com.sap.sailing.landscape.ui.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Named;

public class AwsShardDTO implements IsSerializable, Named {
    private static final long serialVersionUID = 1L;
    ArrayList<String> keys;
    String targetGroupArn;
    String targetGroupName;
    String autoScalingGroupArn;
    String autoScalingGroupName;
    String autoBalancerArn;
    String name;
    String replicaName;

    public AwsShardDTO() {
    };

    public AwsShardDTO(Iterable<String> keys, String targetGroupArn, String targetGroupName, String autoScalinggroupArn,
            String autoBalancerArn, String autoScalingGroupName, String name, String replicaName) {
        this.keys = new ArrayList<String>();
        for (String s : keys) {
            this.keys.add(s);
        }
        this.targetGroupArn = targetGroupArn;
        this.targetGroupName = targetGroupName;
        this.autoBalancerArn = autoBalancerArn;
        this.autoScalingGroupName = autoScalingGroupName;
        this.autoScalingGroupArn = autoScalinggroupArn;
        this.name = name;
        this.replicaName = replicaName;
    }

    public String getReplicaname() {
        return replicaName;
    }

    public void setReplicaname(String replicaname) {
        this.replicaName = replicaname;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    public String getTargetgroupArn() {
        return targetGroupArn;
    }

    public void setTargetgroupArn(String targetgroupArn) {
        this.targetGroupArn = targetgroupArn;
    }

    public String getTargetgroupName() {
        return targetGroupName;
    }

    public void setTargetgroupName(String targetgroupName) {
        this.targetGroupName = targetgroupName;
    }

    public String getAutosclaingGroupArn() {
        return autoScalingGroupArn;
    }

    public void setAutosclaingGroupArn(String autosclaingGroupArn) {
        this.autoScalingGroupArn = autosclaingGroupArn;
    }

    public String getAutobalancerArn() {
        return autoBalancerArn;
    }

    public void setAutobalancerArn(String autobalancerArn) {
        this.autoBalancerArn = autobalancerArn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAutoscalingGroupName() {
        return this.autoScalingGroupName;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getKeysString() {
        return String.join(", ", keys);
    }
}
