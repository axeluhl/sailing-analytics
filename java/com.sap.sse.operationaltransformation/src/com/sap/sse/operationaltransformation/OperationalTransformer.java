package com.sap.sse.operationaltransformation;

/**
 * A default implementation for the {@link Transformer} interface, assuming operations implement the
 * {@link OperationWithTransformationSupport} interface which standardizes how operations are being transformed.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S>
 * @param <O>
 */
public class OperationalTransformer<S, O extends OperationWithTransformationSupport<S, O>> implements Transformer<S, O> {
    @Override
    public ClientServerOperationPair<S, O> transform(O clientOp, O serverOp) {
        ClientServerOperationPair<S, O> result = new ClientServerOperationPair<S, O>(
                clientOp == null ? null : clientOp.transformClientOp(serverOp),
                        serverOp == null ? null : serverOp.transformServerOp(clientOp));
        return result;
    }
}
