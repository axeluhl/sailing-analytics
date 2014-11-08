package com.sap.sse.operationaltransformation;

/**
 * Returns the same state object as was passed in. This is required for the Operational Transformation algorithm to work.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S>
 * @param <O>
 */
public interface OperationWithTransformationSupport<S, O extends OperationWithTransformationSupport<S, O>> extends Operation<S> {
    /**
     * Implements the specific transformation rule for the implementing subclass for the set of possible peer operations
     * along which to transform this operation, assuming this is the client operation. See
     * {@link Transformer#transform(Operation, Operation)} for the specification.
     * 
     * @return the result of transforming <code>this</code> operation along <code>serverOp</code>
     */
    O transformClientOp(O serverOp);

    /**
     * Implements the specific transformation rule for the implementing subclass for the set of possible peer operations
     * along which to transform this operation, assuming this is the server operation. See
     * {@link Transformer#transform(Operation, Operation)} for the specification.
     * 
     * @return the result of transforming <code>this</code> operation along <code>clientOp</code>
     */
    O transformServerOp(O clientOp);
}
