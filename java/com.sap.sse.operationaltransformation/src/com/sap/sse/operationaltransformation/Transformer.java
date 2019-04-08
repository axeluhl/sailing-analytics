package com.sap.sse.operationaltransformation;

public interface Transformer<S, O extends Operation<S>> {
    /**
     * Takes an operation from the client and an operation from the server, both based on the same state. The
     * complementary pair of operations is computed such that if the client applies the server operation returned, and
     * the server applies the client operation returned, both end up in equal states again.
     * <p>
     * 
     * If a resulting operation is supposed to not have any effects, return <code>null</code> as the operations in the
     * resulting pair.
     * 
     * @param clientOp
     *            operation that was executed on the CLIENT; never called with <code>null</code>
     * @param serverOp
     *            operation that was executed on the SERVER; never called with <code>null</code>
     */
    ClientServerOperationPair<S, O> transform(O clientOp, O serverOp);
}
