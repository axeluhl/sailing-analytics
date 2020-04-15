package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class DataImportFailed extends AbstractRacingEventServiceOperation<Void> {

    private static final long serialVersionUID = -3509389848341980239L;
    private UUID importOperationId;
    private String errorMessage;

    public DataImportFailed(UUID importOperationId, String errorMessage) {
        this.importOperationId = importOperationId;
        this.errorMessage = errorMessage;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.setDataImportFailedWithoutReplication(importOperationId, errorMessage);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }

}
