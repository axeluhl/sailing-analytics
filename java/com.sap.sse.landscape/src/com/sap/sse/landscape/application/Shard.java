package com.sap.sse.landscape.application;

/**
 * Part of a {@link Scope}. A {@link Shard} cannot be moved in isolation and can hence not move across {@link Scope}s.
 * Sharing can be used to optionally split {@link ApplicationProcess}es in groups, each of which being responsible
 * primarily only for a subset of the {@link Shard}s available in the {@link ApplicationReplicaSet}. While we assume
 * that all {@link ApplicationProcess}es in an {@link ApplicationReplicaSet} <em>can</em> handle requests for all
 * {@link Shard}s managed by the replica set, it may be beneficial performance-wise to have individual processes
 * focus only on a subset of {@link Shard}s. This may, e.g., result in better cache utilization and hence less CPU
 * consumption on the hosts running those processes.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Shard<ShardingKey> {
    ShardingKey getKey();
}
