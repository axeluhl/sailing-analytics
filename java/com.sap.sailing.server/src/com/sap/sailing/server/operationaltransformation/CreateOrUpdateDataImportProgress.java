package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateOrUpdateDataImportProgress extends AbstractRacingEventServiceOperation<Void> {

    private static final long serialVersionUID = 7619972832594152077L;

    private UUID importOperationId;
    private double overallProgressPct;
    private String subProgressName;
    private double subProgressPct;

    public CreateOrUpdateDataImportProgress(UUID importOperationId, double overallProgressPct, String subProgressName,
            double subProgressPct) {
        this.importOperationId = importOperationId;
        this.overallProgressPct = overallProgressPct;
        this.subProgressName = subProgressName;
        this.subProgressPct = subProgressPct;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.createOrUpdateDataImportProgressWithoutReplication(importOperationId, overallProgressPct,
                subProgressName, subProgressPct);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<Void> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<Void> clientOp) {
        return null;
    }

}
