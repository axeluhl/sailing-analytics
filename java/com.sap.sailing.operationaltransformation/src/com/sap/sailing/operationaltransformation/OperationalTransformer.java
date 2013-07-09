package com.sap.sailing.operationaltransformation;


public class OperationalTransformer<S, O extends OperationWithTransformationSupport<S, O>> implements Transformer<O> {

    @Override
    public ClientServerOperationPair<O> transform(O clientOp, O serverOp) {
        ClientServerOperationPair<O> result = new ClientServerOperationPair<O>(
                clientOp.transformClientOp(serverOp), serverOp.transformServerOp(clientOp));
        return result;
    }

}
