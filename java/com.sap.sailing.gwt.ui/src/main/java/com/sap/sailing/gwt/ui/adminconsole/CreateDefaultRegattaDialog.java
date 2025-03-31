package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CreateDefaultRegattaDialog extends DataEntryDialog<Void> {
    
    public CreateDefaultRegattaDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Void> callback) {
        super(stringMessages.createDefaultRegatta(), /*message*/ stringMessages.doYouWantToCreateADefaultRegatta(), stringMessages.yes(), stringMessages.no(), /*validator*/ null, callback);
    }   

    @Override
    protected Void getResult() {
        return null;
    }
}
