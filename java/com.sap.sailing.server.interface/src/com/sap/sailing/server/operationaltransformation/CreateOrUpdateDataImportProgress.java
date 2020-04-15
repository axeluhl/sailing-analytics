package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class CreateOrUpdateDataImportProgress extends AbstractRacingEventServiceOperation<Void> {

    private static final long serialVersionUID = 1980532937717924118L;

    private UUID importOperationId;
    private double overallProgressPct;
    private DataImportSubProgress subProgress;
    private double subProgressPct;

    public CreateOrUpdateDataImportProgress(UUID importOperationId, double overallProgressPct, 
            DataImportSubProgress subProgress, double subProgressPct) {
        this.importOperationId = importOperationId;
        this.overallProgressPct = overallProgressPct;
        this.subProgress = subProgress;
        this.subProgressPct = subProgressPct;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.createOrUpdateDataImportProgressWithoutReplication(importOperationId, overallProgressPct,
                subProgress, subProgressPct);
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
