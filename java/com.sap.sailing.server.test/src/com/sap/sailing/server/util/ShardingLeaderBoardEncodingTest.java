package com.sap.sailing.server.util;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.common.sharding.ShardingType;

public class ShardingLeaderBoardEncodingTest {
    @Test
    public void testEncoding() {
        Assert.assertEquals("/leaderboard/pureascistring", ShardingType.LEADERBOARDNAME.encodeShardingInfo("pureascistring"));
        Assert.assertEquals("/leaderboard/unpure_asci_string", ShardingType.LEADERBOARDNAME.encodeShardingInfo("unpure asci string"));
        Assert.assertEquals("/leaderboard/c_dille", ShardingType.LEADERBOARDNAME.encodeShardingInfo("cédille"));
        Assert.assertEquals("/leaderboard/Hello_World", ShardingType.LEADERBOARDNAME.encodeShardingInfo("Hello+World"));
        Assert.assertEquals("/leaderboard/Hello_World_", ShardingType.LEADERBOARDNAME.encodeShardingInfo("Hello(World)"));
    }
}
