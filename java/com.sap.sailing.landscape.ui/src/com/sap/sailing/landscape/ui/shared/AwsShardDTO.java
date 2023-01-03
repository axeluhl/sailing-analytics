package com.sap.sailing.landscape.ui.shared;

import java.util.ArrayList;

import com.sap.sse.common.Named;
import com.sap.sse.common.Util;

public class AwsShardDTO implements Named {
    private static final long serialVersionUID = 1L;
    ArrayList<String> leaderboardNames;
    private String targetGroupArn;
    private String targetGroupName;
    private String autoScalingGroupArn;
    private String autoScalingGroupName;
    private String autoBalancerArn;
    private String name;
    private String replicaName;

    @SuppressWarnings("unused") // for GWT serialisation only
    private AwsShardDTO() {
    }

    public AwsShardDTO(Iterable<String> leaderboardNames, String targetGroupArn, String targetGroupName, String autoScalinggroupArn,
            String autoBalancerArn, String autoScalingGroupName, String name, String replicaName) {
        this.leaderboardNames = new ArrayList<>();
        Util.addAll(leaderboardNames, this.leaderboardNames);
        this.targetGroupArn = targetGroupArn;
        this.targetGroupName = targetGroupName;
        this.autoBalancerArn = autoBalancerArn;
        this.autoScalingGroupName = autoScalingGroupName;
        this.autoScalingGroupArn = autoScalinggroupArn;
        this.name = name;
        this.replicaName = replicaName;
    }

    public String getReplicaName() {
        return replicaName;
    }

    public ArrayList<String> getLeaderboardNames() {
        return leaderboardNames;
    }

    public String getTargetgroupArn() {
        return targetGroupArn;
    }

    public String getTargetgroupName() {
        return targetGroupName;
    }

    public String getAutosclaingGroupArn() {
        return autoScalingGroupArn;
    }

    public String getAutobalancerArn() {
        return autoBalancerArn;
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
}
