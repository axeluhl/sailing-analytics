package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.sensordata.KnownSensorDataTypes;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class SensorDataImportWidget extends AbstractFileImportWidget implements IsWidget {

    public SensorDataImportWidget(TrackFileImportDeviceIdentifierTableWrapper table, StringMessages stringMessages,
            final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        super("/sailingserver/sensordata/import", stringMessages.importFixes(), table, sailingService, errorReporter);
        setMultipleFileUploadEnabled(true);
    }

    @Override
    protected void getImporterTypes(AsyncCallback<Collection<String>> callback) {
        callback.onSuccess(Collections.singletonList(KnownSensorDataTypes.BRAVO.name()));
    }

}
