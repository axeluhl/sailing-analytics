package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class CreateDefaultRegattaLeaderboardDialog extends AbstractOkCancelDialog{

    private RegattaDTO regatta;
    
    public CreateDefaultRegattaLeaderboardDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, RegattaDTO regatta) {
        super(sailingService, stringMessages, errorReporter);
        this.regatta = regatta;
        setupUi();
    }   
    
    @Override
    protected void ok() {
        RegattaIdentifier regattaIdentifier = new RegattaName(regatta.getName());
        sailingService.createRegattaLeaderboard(regattaIdentifier, regatta.getName(), new int[]{},
                new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create default regatta leaderboard for " + regatta.getName()
                        + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(StrippedLeaderboardDTO result) {
                hide();
            }
        });
    }

}
