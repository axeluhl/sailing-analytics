package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONParser;

public class SensorDataImportResponse extends JavaScriptObject {
    private static final Logger logger = Logger.getLogger(SensorDataImportResponse.class.getName());

    public static final SensorDataImportResponse parse(String json) {
        try {
            return (SensorDataImportResponse) JSONParser.parseStrict(json).isObject().getJavaScriptObject();
        } catch (Exception e) {
            logger.severe("failed to parse result");
            return null;
        }
    }

    protected SensorDataImportResponse() {
    }

    public final List<String> getUploads() {
        final String[] uploads = uploads();
        if (uploads == null || uploads.length == 0) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(uploads);
        }
    }

    public final List<ErrorMessage> getErrors() {
        final JsArray<ErrorMessage> errors = errors();
        if (errors == null || errors.length() == 0) {
            return Collections.emptyList();
        } else {
            final ArrayList<ErrorMessage> errorsAsList = new ArrayList<>(errors.length());
            for (int i = 0; i < errors.length(); i++) {
                errorsAsList.add(errors.get(i));
            }
            return errorsAsList;
        }
    }

    private final native String[] uploads() /*-{
	return this.uploads;
    }-*/;

    private final native JsArray<ErrorMessage> errors() /*-{
	return this.errors;
    }-*/;

    public final boolean didSucceedImportingAnyFile() {
        return getUploads() != null && getUploads().size() > 0;
    }

    public final boolean hasErrors() {
        final JsArray<ErrorMessage> errors = errors();
        return errors != null && errors.length() > 0;
    }
}

