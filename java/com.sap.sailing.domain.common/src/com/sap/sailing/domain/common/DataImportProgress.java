package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.UUID;

public interface DataImportProgress extends Serializable {

    double getOverallProgressPct();

    void setOverAllProgressPct(double pct);

    String getNameOfCurrentSubProgress();

    void setNameOfCurrentSubProgress(String name);

    double getCurrentSubProgressPct();

    void setCurrentSubProgressPct(double pct);

    /**
     * 
     * @return the result if the operation id done, null otherwise
     */
    MasterDataImportObjectCreationCount getResult();

    void setResult(MasterDataImportObjectCreationCount result);

    UUID getOperationId();

    boolean failed();

    void setFailed();

    String getErrorMessage();

    void setErrorMessage(String message);

}
