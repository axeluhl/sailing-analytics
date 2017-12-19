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

    public final List<ErrorMessage> getErrors() {
        return getJsObjectList("errors");
    }

    public final boolean didSucceedImportingAnyFile() {
        return getUploads() != null && !getUploads().isEmpty();
    }

    public final boolean hasErrors() {
        final List<ErrorMessage> errors = getErrors();
        return errors != null && !errors.isEmpty();
    }
}

