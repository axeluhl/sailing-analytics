package com.sap.sse.landscape.application;

/**
 * A scope that contains data. Scopes are the basis for data partitioning and for moving content across the landscape.
 * When not currently "in transit," a scope is expected to be reachable on exactly one {@link ApplicationReplicaSet}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Scope<ShardingKey> {
    /**
     * The elements inside this scope that can be the basis for "sharding" which means to split primary responsibility
     * for handling requests for a {@link Shard} across the {@link ApplicationProcess}es hosting this scope.
     */
    Iterable<Shard<ShardingKey>> getShards();
}
