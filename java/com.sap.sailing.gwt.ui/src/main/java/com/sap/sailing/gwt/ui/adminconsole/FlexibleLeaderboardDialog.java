package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public abstract class FlexibleLeaderboardDialog extends AbstractLeaderboardDialog {
    protected ListBox scoringSchemeListBox;

    protected static class LeaderboardParameterValidator implements Validator<LeaderboardDescriptor> {
        protected final StringMessages stringConstants;
        protected final Collection<StrippedLeaderboardDTO> existingLeaderboards;
        
        public LeaderboardParameterValidator(StringMessages stringConstants, Collection<StrippedLeaderboardDTO> existingLeaderboards){
            super();
            this.stringConstants = stringConstants;
            this.existingLeaderboards = existingLeaderboards;
        }

        @Override
        public String getErrorMessage(LeaderboardDescriptor leaderboardToValidate) {
            String errorMessage;
            boolean nonEmpty = leaderboardToValidate.getName() != null && leaderboardToValidate.getName().length() > 0;

            boolean discardThresholdsAscending = true;
            for (int i = 1; i < leaderboardToValidate.getDiscardThresholds().length; i++) {
                // TODO what are correct values for discarding Thresholds?
                if (0 < leaderboardToValidate.getDiscardThresholds().length){ 
                    discardThresholdsAscending = discardThresholdsAscending
                            && leaderboardToValidate.getDiscardThresholds()[i - 1] < leaderboardToValidate.getDiscardThresholds()[i]
                    // and if one box is empty, all subsequent boxes need to be empty too
                    && (leaderboardToValidate.getDiscardThresholds()[i] == 0 || leaderboardToValidate.getDiscardThresholds()[i-1] > 0);
                }
            }
            
            boolean unique = true;
            for (StrippedLeaderboardDTO dao : existingLeaderboards) {
                if(dao.name.equals(leaderboardToValidate.getName())){
                    unique = false;
                }
            }
            
            if (!nonEmpty) {
                errorMessage = stringConstants.pleaseEnterAName();
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
    
    public FlexibleLeaderboardDialog(String title, LeaderboardDescriptor leaderboardDTO, StringMessages stringMessages,
            ErrorReporter errorReporter, LeaderboardParameterValidator validator,  AsyncCallback<LeaderboardDescriptor> callback) {
        super(title, leaderboardDTO, stringMessages, validator, callback);
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
        LeaderboardDescriptor leaderboard = super.getResult();
        leaderboard.setRegattaName(null);
        leaderboard.setScoringScheme(getSelectedScoringSchemeType(scoringSchemeListBox, stringMessages));
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(3,2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0,  0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameTextBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.scoringSystem() + ":"));
        formGrid.setWidget(1, 1, scoringSchemeListBox);
        mainPanel.add(formGrid);
        mainPanel.add(new Label(stringMessages.discardRacesFromHowManyStartedRacesOn()));
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
