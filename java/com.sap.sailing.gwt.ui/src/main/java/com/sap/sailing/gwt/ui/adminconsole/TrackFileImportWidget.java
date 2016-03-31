package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class TrackFileImportWidget extends AbstractFileImportWidget implements IsWidget {
   
    public TrackFileImportWidget(TrackFileImportDeviceIdentifierTableWrapper table, StringMessages stringMessages,
            final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        super("/sailingserver/trackfiles/import", stringMessages.importFixes(), table, sailingService, errorReporter);
    }
    
    @Override
    protected void getImporterTypes(AsyncCallback<Collection<String>> callback) {
        sailingService.getGPSFixImporterTypes(callback);
    }
    
}
