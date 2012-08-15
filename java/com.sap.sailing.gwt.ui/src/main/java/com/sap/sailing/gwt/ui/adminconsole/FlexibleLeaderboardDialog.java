package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public abstract class FlexibleLeaderboardDialog extends AbstractLeaderboardDialog {

    protected static class LeaderboardParameterValidator implements Validator<StrippedLeaderboardDTO> {
        protected final StringMessages stringConstants;
        protected final Collection<StrippedLeaderboardDTO> existingLeaderboards;
        
        public LeaderboardParameterValidator(StringMessages stringConstants, Collection<StrippedLeaderboardDTO> existingLeaderboards){
            super();
            this.stringConstants = stringConstants;
            this.existingLeaderboards = existingLeaderboards;
        }

        @Override
        public String getErrorMessage(StrippedLeaderboardDTO leaderboardToValidate) {
            String errorMessage;
            boolean nonEmpty = leaderboardToValidate.name != null && leaderboardToValidate.name.length() > 0;

            boolean discardThresholdsAscending = true;
            for (int i = 1; i < leaderboardToValidate.discardThresholds.length; i++) {
                // TODO what are correct values for discarding Thresholds?
                if (0 < leaderboardToValidate.discardThresholds.length){ 
                    discardThresholdsAscending = discardThresholdsAscending
                            && leaderboardToValidate.discardThresholds[i - 1] < leaderboardToValidate.discardThresholds[i]
                    // and if one box is empty, all subsequent boxes need to be empty too
                    && (leaderboardToValidate.discardThresholds[i] == 0 || leaderboardToValidate.discardThresholds[i-1] > 0);
                }
            }
            
            boolean unique = true;
            for (StrippedLeaderboardDTO dao : existingLeaderboards) {
                if(dao.name.equals(leaderboardToValidate.name)){
                    unique = false;
                }
            }
            
            if (!nonEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if(!unique){
                errorMessage = stringConstants.leaderboardWithThisNameAlreadyExists();
            } else if (!discardThresholdsAscending) {
                errorMessage = stringConstants.discardThresholdsMustBeAscending();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }
    
    public FlexibleLeaderboardDialog(StrippedLeaderboardDTO leaderboardDTO, StringMessages stringConstants,
            ErrorReporter errorReporter, LeaderboardParameterValidator validator,  AsyncCallback<StrippedLeaderboardDTO> callback) {
        super(stringConstants.createFlexibleLeaderboard(), leaderboardDTO, stringConstants, validator, callback);
    }
    
    @Override
    protected StrippedLeaderboardDTO getResult() {
        StrippedLeaderboardDTO leaderboard = super.getResult();
        leaderboard.regatta = null;
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        
        Grid formGrid = new Grid(3,2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0,  0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameTextBox);
        formGrid.setWidget(1, 0, new Label(stringConstants.scoringSystem() + ":"));
        formGrid.setWidget(1, 1, scoringSchemeListBox);
                
        mainPanel.add(formGrid);
        
        mainPanel.add(new Label(stringConstants.discardRacesFromHowManyStartedRacesOn()));
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(3);
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            hp.add(new Label("" + (i + 1) + "."));
            hp.add(discardThresholdBoxes[i]);
        }
        mainPanel.add(hp);
        return mainPanel;
    }  
}
