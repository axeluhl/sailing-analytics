package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CreateDefaultLeaderboardGroupDialog extends DataEntryDialog<Void>{
    
    public CreateDefaultLeaderboardGroupDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Void> dialogCallBack) {
        super(stringMessages.createDefaultLeaderboardGroup(), /*message*/ stringMessages.createDefaultLeaderboardGroup(), stringMessages.yes(), stringMessages.no(), /*validator*/ null, dialogCallBack);
    }   

    @Override
    protected Void getResult() {
        return null;
    }

}
