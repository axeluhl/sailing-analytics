package com.sap.sailing.server.util;

import org.junit.Test;

import com.sap.sailing.domain.common.sharding.ShardingType;

import junit.framework.Assert;

public class ShardingLeaderBoardEncodingTest {
    @Test
    public void testEncoding() {
        Assert.assertEquals("/leaderboard/pureascistring", ShardingType.LEADERBOARDNAME.encodeIfNeeded("pureascistring"));
        Assert.assertEquals("/leaderboard/unpure_asci_string", ShardingType.LEADERBOARDNAME.encodeIfNeeded("unpure asci string"));
        Assert.assertEquals("/leaderboard/c_dille", ShardingType.LEADERBOARDNAME.encodeIfNeeded("cédille"));
        Assert.assertEquals("/leaderboard/Hello_World", ShardingType.LEADERBOARDNAME.encodeIfNeeded("Hello+World"));
        Assert.assertEquals("/leaderboard/Hello_World_", ShardingType.LEADERBOARDNAME.encodeIfNeeded("Hello(World)"));
    }
}
