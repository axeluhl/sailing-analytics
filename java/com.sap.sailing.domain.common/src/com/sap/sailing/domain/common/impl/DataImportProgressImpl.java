package com.sap.sailing.domain.common.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;

public class DataImportProgressImpl implements DataImportProgress {

    private static final long serialVersionUID = 5538458397711003527L;
    private UUID currentImportOperationId;
    private MasterDataImportObjectCreationCount result = null;
    private double overallProgressPct = 0;
    private DataImportSubProgress currentSubProgress = DataImportSubProgress.IMPORT_INIT;
    private double currentSubProgressPct = 0;
    private boolean failed = false;
    private String errorMessage;

    DataImportProgressImpl() {
    }

    public DataImportProgressImpl(final UUID currentImportOperationId) {
        this.currentImportOperationId = currentImportOperationId;
    }

    @Override
    public double getOverallProgressPct() {
        return overallProgressPct;
    }
    
    @Override
    public DataImportSubProgress getCurrentSubProgress() {
        return currentSubProgress;
    }

    @Override
    public double getCurrentSubProgressPct() {
        return currentSubProgressPct;
    }

    @Override
    public MasterDataImportObjectCreationCount getResult() {
        return result;
    }

    @Override
    public void setOverAllProgressPct(double pct) {
        overallProgressPct = pct;
    }
    
    @Override
    public void setCurrentSubProgress(DataImportSubProgress subProgress) {
        currentSubProgress = subProgress;
    }

    @Override
    public void setCurrentSubProgressPct(double pct) {
        currentSubProgressPct = pct;
    }

    @Override
    public void setResult(MasterDataImportObjectCreationCount result) {
        this.result = result;
    }

    @Override
    public UUID getOperationId() {
        return currentImportOperationId;
    }

    @Override
    public boolean failed() {
        return failed;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setFailed() {
        failed = true;
    }

    @Override
    public void setErrorMessage(String message) {
        errorMessage = message;
    }

}
