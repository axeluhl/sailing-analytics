package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class TrackFileImportWidget extends AbstractFileImportWidget implements IsWidget {
   
    public TrackFileImportWidget(TrackFileImportDeviceIdentifierTableWrapper table, StringMessages stringMessages,
            final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        super("/sailingserver/trackfiles/import", stringMessages, table, sailingService, errorReporter);
        setDownsampleOptionVisible(false);
    }
    
    @Override
    protected void getImporterTypes(final AsyncCallback<Collection<String>> callback) {
        sailingService.getGPSFixImporterTypes(new AsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(Collection<String> result) {
                ArrayList<String> importerTypes = new ArrayList<String>(result.size() + 1);
                importerTypes.add("");
                importerTypes.addAll(result);
                callback.onSuccess(importerTypes);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }
    
}
