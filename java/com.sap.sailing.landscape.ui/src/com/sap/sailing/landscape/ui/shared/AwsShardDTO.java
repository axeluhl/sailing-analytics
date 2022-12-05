package com.sap.sailing.landscape.ui.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AwsShardDTO implements  IsSerializable {
    ArrayList<String> keys;
    String targetgroupArn;
    String targetgroupName;
    String autosclaingGroupArn;
    String autoscalingGroupName;
    String autobalancerArn;
    String name;
    String replicaname;
    
    

    public AwsShardDTO(){};
    
    public AwsShardDTO(Iterable<String> keys, String targetgroupArn,
            String targetgroupName, String autoscalinggroupArn, String autobalancerArn,String autoScalingGroupName, String name, String replicaname){
        this.keys = new ArrayList<String>();
        for(String s : keys) {
            this.keys.add(s);
        }
        this.targetgroupArn  =targetgroupArn;
        this.targetgroupName = targetgroupName;
        this.autobalancerArn = autobalancerArn;
        this.autoscalingGroupName = autoScalingGroupName;
        this.autosclaingGroupArn = autoscalinggroupArn;
        this.name = name;
        this.replicaname  =replicaname;
        
    }

    public String getReplicaname() {
        return replicaname;
    }

    public void setReplicaname(String replicaname) {
        this.replicaname = replicaname;
    }
    
    
    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    public String getTargetgroupArn() {
        return targetgroupArn;
    }

    public void setTargetgroupArn(String targetgroupArn) {
        this.targetgroupArn = targetgroupArn;
    }

    public String getTargetgroupName() {
        return targetgroupName;
    }

    public void setTargetgroupName(String targetgroupName) {
        this.targetgroupName = targetgroupName;
    }

    public String getAutosclaingGroupArn() {
        return autosclaingGroupArn;
    }

    public void setAutosclaingGroupArn(String autosclaingGroupArn) {
        this.autosclaingGroupArn = autosclaingGroupArn;
    }

    public String getAutobalancerArn() {
        return autobalancerArn;
    }

    public void setAutobalancerArn(String autobalancerArn) {
        this.autobalancerArn = autobalancerArn;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getAutoscalingGroupName() {
        return this.autoscalingGroupName;
    }

    public String getName() {
        return name;
    }
    
    public String getKeysString() {
        String s  ="";
        for(String i : getKeys()) {
            s = s + i + ", " ;
        }
        return s;
    }

}
