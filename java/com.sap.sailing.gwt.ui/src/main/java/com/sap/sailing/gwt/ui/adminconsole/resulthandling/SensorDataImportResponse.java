package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.List;

import com.google.gwt.core.client.JsArray;

public class SensorDataImportResponse extends AbstractDataImportResponse {

    public static final SensorDataImportResponse parse(String json) {
        return AbstractDataImportResponse.parse(json, SensorDataImportResponse.class);
    }

    protected SensorDataImportResponse() {
    }

    public final List<String> getUploads() {
        return asList(this.uploads());
    }

    public final List<ErrorMessage> getErrors() {
        return asList(this.errors());
    }

    private final native String[] uploads() /*-{
        return this.uploads;
    }-*/;

    private final native JsArray<ErrorMessage> errors() /*-{
        return this.errors;
    }-*/;

    public final boolean didSucceedImportingAnyFile() {
        return getUploads() != null && !getUploads().isEmpty();
    }

    public final boolean hasErrors() {
        final JsArray<ErrorMessage> errors = errors();
        return errors != null && errors.length() > 0;
    }
}

