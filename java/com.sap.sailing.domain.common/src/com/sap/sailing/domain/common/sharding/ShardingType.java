package com.sap.sailing.domain.common.sharding;

/**
 * Enum that defines all sharding types known by the system.
 * 
 * For a sharding to work, we need both client and backend properly identifying a shard.
 * 
 * While the client has to properly deliver sharding information on each request, the server side
 * infrastructure must be able to identify the information and properly route the requests.
 * 
 * The current implementation uses a SUFFIX appended the GWT RPC URLs (allowing path based routing), so that each call to the server
 * propagates the sharding information through the url.
 * 
 * A load balancer can be used to properly route each request to the corresponding server node. The first
 * sharding implemented is the sharding by leaderboard name.
 * 
 * The suffix appended to the request URL consists of two parts: a prefix that is used to identify the type of shard
 * and a payload that will be used to identify the shard.
 * 
 *
 */
public enum ShardingType {
    LEADERBOARDNAME("/leaderboard/");

    private String prefix;

    private ShardingType(String prefix) {
        this.prefix = prefix;
    }

    public String encodeIfNeeded(String shardingInfo) {
        if (shardingInfo.startsWith(prefix)) {
            return shardingInfo;
        }
        return new StringBuilder().append(prefix)
                .append(normalize(shardingInfo))
                .toString();
    }

    private String normalize(String replace) {
        char[] chars = replace.toCharArray();
        StringBuilder answer = new StringBuilder();
        for(int i = 0;i<chars.length;i++) {
            char c = chars[i];
            if (c > 47 && c < 58) { // digits
                answer.append(c);
            } else if (c > 64 && c < 91) { // uppercase letters
                answer.append(c);
            } else if (c > 69 && c < 123) { // lowercase letters
                answer.append(c);
            } else {
                answer.append("_");
            }
        }
        return answer.toString();
    }

    public String getPrefix() {
        return prefix;
    }
}