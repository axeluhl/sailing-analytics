package com.sap.sailing.domain.sharding;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sailing.domain.leaderboard.caching.LiveLeaderboardUpdater;

/**
 * The sharding context is used to store and verify contraints for a thread.
 * 
 * This is currently used to verify if a given thread, that is explicitly expected to only 
 * trigger the calculation of a given leaderboard, does trigger the calculation of further/ other
 * leaderboards.
 * 
 * It is also used to log warnings if a leaderboard is calculated without predefining the thread constraint.  
 *
 * The enum {@link ShardingType} lists the known sharding types. Each sharding type defines a prefix that will
 * be used to identify the sharding type from the request, as implemented in  {@link #identifyAndSetShardingConstraint(String)}.
 * 
 * By calling {@link #setShardingConstraint(ShardingType, String)} it is possible to directly set a thread constraint. 
 * This is useful in background computing code, as in {@link LiveLeaderboardUpdater} 
 *  
 */
public class ShardingContext {
    private static final Logger logger = Logger.getLogger(ShardingContext.class.getName());

    private static final ConcurrentMap<ShardingType, ThreadLocal<String>> shardingMap = new ConcurrentHashMap<>();

    /**
     * Sets the sharding context. The type of sharding will be identified by the prefix.
     * 
     * @param shardingInfo
     */
    public static ShardingType identifyAndSetShardingConstraint(String shardingInfo) {
        if (shardingInfo == null || shardingInfo.isEmpty()) {
            return null;
        }
        ThreadLocal<String> identifiedShardingHolder = null;
        for (ShardingType shardingType : ShardingType.values()) {
            if (shardingInfo.startsWith(shardingType.getPrefix())) {
                identifiedShardingHolder = shardingMap.computeIfAbsent(shardingType, t -> new ThreadLocal<>());
                checkAndSetShardingInfo(shardingType, shardingInfo, identifiedShardingHolder);
                return shardingType;
            }
        }
        if (identifiedShardingHolder == null) {
            logger.warning("Could not identify sharding type for: " + shardingInfo);
        }
        return null;
    }

    /**
     * Provide sharding constraint for given sharding type.
     * 
     * @param shardingType
     * @param shardingInfo
     */
    public static void setShardingConstraint(ShardingType shardingType, String shardingInfo) {
        final String encodedShardingInfo = shardingType.encodeIfNeeded(shardingInfo);
        ThreadLocal<String> shardingHolder = shardingMap.computeIfAbsent(shardingType, t -> new ThreadLocal<>());
        checkAndSetShardingInfo(shardingType, encodedShardingInfo, shardingHolder);
    }

    /**
     * Check if provided sharding information name equals shard constraint.
     * 
     * If the information is null or empty, we log a warning. 
     * If shard does not have a constraint, we log a warning. If
     * the information provided does not match information previously stored in shard, 
     * we make a severe log entry.
     * 
     * @param shardingInfo
     */
    public static void checkConstraint(final ShardingType type, final String shardingInfo) {
        if (shardingInfo == null || shardingInfo.isEmpty()) {
            logger.warning("Empty sharding constraint");
            return;
        }
        final ThreadLocal<String> shardingHolder = shardingMap.get(type);
        if (shardingHolder == null) {
            logger.log(Level.WARNING, "No current sharding context set for " + type.name(), new RuntimeException());
            return;
        }
        String currentShardingInfo = shardingHolder.get();
        if (currentShardingInfo == null || currentShardingInfo.isEmpty()) {
            logger.warning("No current sharding constraint for " + type.name());
            return;
        }
        final String encodedShardingInfo = type.encodeIfNeeded(shardingInfo);
        if (!encodedShardingInfo.equals(currentShardingInfo)) {
            logger.log(Level.SEVERE, "Current sharding constraint vialation for " + type.name() + ". Got "
                    + shardingInfo + ", shard requires " + currentShardingInfo, new RuntimeException());
            return;
        }
    }

    public static void clearShardingConstraint(ShardingType type) {
        ThreadLocal<String> shardingHolder = shardingMap.get(type);
        if (shardingHolder != null) {
            shardingHolder.remove();
        }
    }

    private static void checkAndSetShardingInfo(ShardingType shardingType, String encodedShardingInfo,
            ThreadLocal<String> shardingHolder) {
        if (encodedShardingInfo == null || encodedShardingInfo.isEmpty()) {
            logger.log(Level.SEVERE, "Cannot set empty sharding information for " + shardingType.name(),
                    new RuntimeException());
        }
        String currentShardingInfo = shardingHolder.get();
        if (currentShardingInfo == null) {
            shardingHolder.set(encodedShardingInfo);
        } else if (!encodedShardingInfo.equals(currentShardingInfo)) {
            logger.log(Level.SEVERE, "Switching shard constraint for " + shardingType.name()+". Got <<" + encodedShardingInfo +">>, expeted <<"+ currentShardingInfo+">>", new RuntimeException());
        }
    }
}
