package com.sap.sailing.gwt.ui.leaderboardedit;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;

public class MatchAndApplyScoreCorrectionsDialog extends DataEntryDialog<ScoreCorrectionsApplicationInstructions> {

    public MatchAndApplyScoreCorrectionsDialog(LeaderboardDTO leaderboard, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, RegattaScoreCorrectionDTO result) {
        super(stringMessages.assignRaceNumbersToRaceColumns(), stringMessages.assignRaceNumbersToRaceColumns(),
                stringMessages.ok(), stringMessages.cancel(), new Validator(), new Callback());
        createAdditionalWidget(result);
    }

    private void createAdditionalWidget(RegattaScoreCorrectionDTO result) {
        // TODO Auto-generated method stub
    }

    @Override
    protected ScoreCorrectionsApplicationInstructions getResult() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        // TODO Auto-generated method stub
        return super.getAdditionalWidget();
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
