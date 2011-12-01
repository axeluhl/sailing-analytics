package com.sap.sailing.gwt.ui.client;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IntegerBox;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

public class LeaderboardCreateDialog extends LeaderboardDialog{
    
    private static class LeaderboardCreateValidator extends LeaderboardParameterValidator{
        private Collection<String> existingLeaderboardNames;
        
        public LeaderboardCreateValidator(Collection<String> existingLeaderboardNames, StringConstants stringConstants) {
            super(stringConstants);
            this.existingLeaderboardNames = existingLeaderboardNames;
        }
        
        @Override
        public String getErrorMessage(LeaderboardDAO valueToValidate) {
            String errorMessage  = super.getErrorMessage(valueToValidate);
            boolean unique = !existingLeaderboardNames.contains(valueToValidate.name);
            if(errorMessage==null && !unique){
                errorMessage = stringConstants.leaderboardWithThisNameAlreadyExists();
            }
            return errorMessage;
        }
        
    }
    
    public LeaderboardCreateDialog(Collection<String> existingLeaderboardNames, StringConstants stringConstants,
            ErrorReporter errorReporter, AsyncCallback<LeaderboardDAO> callback) {
        super(new LeaderboardDAO(), stringConstants, errorReporter, new LeaderboardCreateValidator(existingLeaderboardNames, stringConstants), callback);

        entryField = createTextBox(null);

        discardThresholdBoxes = new IntegerBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            discardThresholdBoxes[i] = createIntegerBoxWithOptionalValue(null, 2);
            discardThresholdBoxes[i].setVisibleLength(2);
        }
    }

}
