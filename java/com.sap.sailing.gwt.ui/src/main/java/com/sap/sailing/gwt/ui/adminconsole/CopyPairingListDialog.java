package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CopyPairingListDialog extends DataEntryDialog<com.sap.sailing.gwt.ui.adminconsole.CopyPairingListDialog.Result> {
    public static class Result {
        
    }
    
    private static class Validator implements DataEntryDialog.Validator<Result> {
        @Override
        public String getErrorMessage(Result valueToValidate) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    public CopyPairingListDialog(StrippedLeaderboardDTO leaderboardDTO, StringMessages stringMessages,
            DialogCallback<Result> dialogCallback) {
        super(stringMessages.copyPairingListFromOtherLeaderboard(), null, stringMessages.ok(), stringMessages.cancel(), new Validator(), dialogCallback);
        // TODO
    }

    @Override
    protected Result getResult() {
        // TODO Auto-generated method stub
        return new Result();
    }

}
