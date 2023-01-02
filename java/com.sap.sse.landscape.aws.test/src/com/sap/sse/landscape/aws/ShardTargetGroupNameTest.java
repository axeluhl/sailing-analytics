package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ShardTargetGroupNameTest {
    @Test
    public void testCreateAndParse() {
        final String replicaSetName = "abc";
        final String shardName = "def";
        final ShardTargetGroupName theShardName = ShardTargetGroupName.create(replicaSetName, shardName);
        final ShardTargetGroupName parsedShardName = ShardTargetGroupName.parse(theShardName.getTargetgroupName(), null);
        assertEquals(shardName, parsedShardName.getShardName());
        assertEquals(theShardName.getTargetgroupName(), parsedShardName.getTargetgroupName());
    }

    @Test
    public void testCreateAndParseLongShardName() {
        final String replicaSetName = "abc";
        final String shardName = "deflawencawueclkajhfalskjfhlaskjdhfllakj";
        final ShardTargetGroupName theShardName = ShardTargetGroupName.create(replicaSetName, shardName);
        final ShardTargetGroupName parsedShardName = ShardTargetGroupName.parse(theShardName.getTargetgroupName(), null);
        assertEquals(theShardName.getTargetgroupName(), parsedShardName.getTargetgroupName());
        final String shardNamePrefix = parsedShardName.getShardName().substring(0,
                parsedShardName.getShardName().indexOf(ShardTargetGroupName.NAMESEPARATOR));
        final String shardNameSuffix = parsedShardName.getShardName()
                .substring(parsedShardName.getShardName().indexOf(ShardTargetGroupName.NAMESEPARATOR)
                        + ShardTargetGroupName.NAMESEPARATOR.length());
        assertTrue(shardName.startsWith(shardNamePrefix));
        assertTrue(shardName.endsWith(shardNameSuffix));
    }
}
