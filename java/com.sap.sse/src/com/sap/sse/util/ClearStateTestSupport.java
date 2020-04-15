package com.sap.sse.util;

public interface ClearStateTestSupport {
    /**
     * Wipes out the complete state. This method is only used for testing. This also clears the persistent state
     * accordingly which differs from the {@link Replicable#clearReplicaState()} operation which doesn't care about the
     * persistent state.<p>
     * 
     * Note: Simple properties like the time delay to the 'live' time point are not reset.
     */
    void clearState() throws Exception;
}
