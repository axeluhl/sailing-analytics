package com.sap.sailing.domain.common.dto;

/**
 * Marker interface for GWT RPC result objects whose serialized representation may be cached. It will
 * be judged by {@link Object#hashCode()} / {@link Object#equals(Object)} whether the serialized representation
 * can be re-used. This means that if the implementing class does not define those methods, Java object identity
 * will be used to decide.
 */
public interface CachableRPCResult {
}
