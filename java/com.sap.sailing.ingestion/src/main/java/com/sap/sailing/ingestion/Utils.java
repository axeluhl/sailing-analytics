package com.sap.sailing.ingestion;

import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import com.sap.sailing.ingestion.dto.EndpointDTO;

public class Utils {
    public static RMap<String, List<EndpointDTO>> getCacheMap() {
        final Config redisConfiguration = new Config();
        redisConfiguration.useReplicatedServers().addNodeAddress(Configuration.REDIS_ENDPOINTS);
        final RedissonClient redisClient = Redisson.create(redisConfiguration);
        return redisClient.getMap(Configuration.REDIS_MAP_NAME);
    }
}
