package com.sap.sailing.gwt.ui.leaderboardedit;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionDTO;

public class MatchAndApplyScoreCorrectionsDialog extends DataEntryDialog<ScoreCorrectionsApplicationInstructions> {

    public MatchAndApplyScoreCorrectionsDialog(String title, String message, String okButtonName,
            String cancelButtonName,
            com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator<ScoreCorrectionsApplicationInstructions> validator,
            AsyncCallback<ScoreCorrectionsApplicationInstructions> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        // TODO Auto-generated constructor stub
    }

    public MatchAndApplyScoreCorrectionsDialog(LeaderboardDTO leaderboard, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, ScoreCorrectionDTO result) {
        super(stringMessages.assignRaceNumbersToRaceColumns(), stringMessages.assignRaceNumbersToRaceColumns(),
                stringMessages.ok(), stringMessages.cancel(), new Validator(), new Callback());
        // TODO Auto-generated constructor stub
    }

    @Override
    protected ScoreCorrectionsApplicationInstructions getResult() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static class Validator implements DataEntryDialog.Validator<ScoreCorrectionsApplicationInstructions> {
        @Override
        public String getErrorMessage(ScoreCorrectionsApplicationInstructions valueToValidate) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static class Callback implements AsyncCallback<ScoreCorrectionsApplicationInstructions> {
        @Override
        public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSuccess(ScoreCorrectionsApplicationInstructions result) {
            // TODO Auto-generated method stub
        }
    }
}
