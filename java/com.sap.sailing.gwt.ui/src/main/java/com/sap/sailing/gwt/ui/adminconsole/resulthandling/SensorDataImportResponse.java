package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.List;

public class SensorDataImportResponse extends AbstractDataImportResponse {

    public static final SensorDataImportResponse parse(String json) {
        return AbstractDataImportResponse.parse(json, "SensorDataImportResponse");
    }

    protected SensorDataImportResponse() {
    }

    public final List<String> getUploads() {
        return getStringList("uploads");
    }

    public final boolean didSucceedImportingAnyFile() {
        return getUploads() != null && !getUploads().isEmpty();
    }

}

