package com.sap.sse.common;

/**
 * Marker interface for GWT RPC result objects whose serialized representation may be cached. It will
 * be judged by object identity, regardless of whether the class implementing this interface offers
 * a dedicated {@link #equals(Object)} and {@link #hashCode()} implementation.
 */
public interface CacheableRPCResult {
}
