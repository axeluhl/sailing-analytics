package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CreateDefaultRegattaLeaderboardDialog extends DataEntryDialog<RegattaName> {

    private RegattaDTO regatta;
    
    public CreateDefaultRegattaLeaderboardDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, RegattaDTO regatta, DialogCallback<RegattaName> callback) {
        super(stringMessages.createDefaultRegattaLeaderboard(), /*message*/ stringMessages.doYouWantToCreateADefaultRegattaLeaderboard(), stringMessages.yes(), stringMessages.no(), /*validator*/ null, callback);
        this.regatta = regatta;
    }   

    @Override
    protected RegattaName getResult() {
        return new RegattaName(regatta.getName());
    }

}
