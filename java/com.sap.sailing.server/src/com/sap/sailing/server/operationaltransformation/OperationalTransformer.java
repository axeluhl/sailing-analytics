package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.ClientServerOperationPair;
import com.sap.sailing.operationaltransformation.Transformer;

public class OperationalTransformer implements Transformer<RacingEventServiceOperation> {

    @Override
    public ClientServerOperationPair<RacingEventServiceOperation> transform(RacingEventServiceOperation clientOp,
            RacingEventServiceOperation serverOp) {
        ClientServerOperationPair<RacingEventServiceOperation> result = new ClientServerOperationPair<RacingEventServiceOperation>(
                clientOp.transformClientOp(serverOp), serverOp.transformServerOp(clientOp));
        return result;
    }

}
