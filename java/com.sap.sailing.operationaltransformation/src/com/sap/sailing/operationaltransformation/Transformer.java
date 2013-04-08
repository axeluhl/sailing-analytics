package com.sap.sailing.operationaltransformation;

public interface Transformer<O extends Operation<?>> {
    /**
     * Takes an operation from the client and an operation from the server, both
     * based on the same state. The complementary pair of operations is computed
     * such that if the client applies the server operation returned, and the
     * server applies the client operation returned, both end up in equal states
     * again.<p>
     * 
     * If a resulting operation is supposed to not have any effects, return the
     * {@link AbstractRacingEventServiceOperation#getNoOp()} operation, but never <code>null</code>
     * as any of the operations in the resulting pair.
     */
    ClientServerOperationPair<O> transform(O transformedOp, O unconfirmedOperation);
}
