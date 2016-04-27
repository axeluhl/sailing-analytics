package com.sap.sailing.domain.common.impl;

import static com.sap.sailing.domain.common.DataImportProgress.SubProgress.IMPORT_INIT;

import java.util.UUID;

import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;

public class DataImportProgressImpl implements DataImportProgress {

    private static final long serialVersionUID = 5538458397711003527L;
    private UUID currentImportOperationId;
    private MasterDataImportObjectCreationCount result = null;
    private double overallProgressPct = 0;
    private String currentSubProgressName = IMPORT_INIT.getMessageKey();
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
    public String getNameOfCurrentSubProgress() {
        return currentSubProgressName;
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
    public void setNameOfCurrentSubProgress(String name) {
        currentSubProgressName = name;
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
