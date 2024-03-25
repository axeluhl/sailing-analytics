package com.sap.sailing.ingestion;

import java.util.Arrays;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import com.sap.sailing.ingestion.dto.EndpointDTO;

public class RedisUtils {
    final static RedissonClient redisClient;
    
    static {
        final Config redisConfiguration = new Config();
        redisConfiguration.useClusterServers().setNodeAddresses(Arrays.asList(Configuration.REDIS_ENDPOINTS));
        redisClient = Redisson.create(redisConfiguration);
    }

    public static RMap<String, List<EndpointDTO>> getCacheMap() {
        return redisClient.getMap(Configuration.REDIS_MAP_NAME);
    }
}
