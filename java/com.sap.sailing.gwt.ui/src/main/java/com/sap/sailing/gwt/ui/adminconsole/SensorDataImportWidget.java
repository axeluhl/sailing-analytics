package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class SensorDataImportWidget extends AbstractFileImportWidget implements IsWidget {

    public SensorDataImportWidget(TrackFileImportDeviceIdentifierTableWrapper table, StringMessages stringMessages,
            final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        super("/sailingserver/sensordata/import", stringMessages, table, sailingService, errorReporter);
        setMultipleFileUploadEnabled(true);
        setDownsampleOptionVisible(true);
    }

    @Override
    protected void getImporterTypes(AsyncCallback<Collection<String>> callback) {
        sailingService.getSensorDataImporterTypes(callback);
    }
    
    public String getSelectedImporterType() {
        return preferredImporterUi.getSelectedValue();
    }

}
