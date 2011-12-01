package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;


public abstract class LeaderboardDialog extends DataEntryDialog<LeaderboardDAO> {
    protected final StringConstants stringConstants;
    protected TextBox entryField;
    protected LeaderboardDAO leaderboard;
    
    protected IntegerBox[] discardThresholdBoxes;
    protected static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

    protected static class LeaderboardParameterValidator implements Validator<LeaderboardDAO> {
        protected final StringConstants stringConstants;
        
        public LeaderboardParameterValidator(StringConstants stringConstants){
            super();
            this.stringConstants = stringConstants;
        }

        @Override
        public String getErrorMessage(LeaderboardDAO leaderboardToValidate) {
            String errorMessage;
            boolean nonEmpty = leaderboardToValidate.name != null && leaderboardToValidate.name.length() > 0;

            boolean discardThresholdsAscending = true;
            for (int i = 1; i < leaderboardToValidate.discardThresholds.length; i++) {
                // TODO what are correct values for discarding Thresholds?
                if (0 < leaderboardToValidate.discardThresholds.length){ 
                    discardThresholdsAscending = discardThresholdsAscending
                            && leaderboardToValidate.discardThresholds[i - 1] < leaderboardToValidate.discardThresholds[i];
                }
            }
            if (!nonEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!discardThresholdsAscending) {
                errorMessage = stringConstants.discardThresholdsMustBeAscending();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }
    
    public LeaderboardDialog(LeaderboardDAO leaderboardDAO, StringConstants stringConstants,
            ErrorReporter errorReporter, LeaderboardParameterValidator validator,  AsyncCallback<LeaderboardDAO> callback) {
        super(stringConstants.leaderboardName(), stringConstants.leaderboardName(), stringConstants.ok(),
                stringConstants.cancel(), validator, callback);
        this.stringConstants = stringConstants;
        this.leaderboard = leaderboardDAO;
    }
    
    @Override
    protected LeaderboardDAO getResult() {
        List<Integer> discardThresholds = new ArrayList<Integer>();
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            if (discardThresholdBoxes[i].getValue() != null
                    && discardThresholdBoxes[i].getValue().toString().length() > 0) {
                discardThresholds.add(discardThresholdBoxes[i].getValue());
            }
        }
        int[] discardThresholdsBoxContents = new int[discardThresholds.size()];
        for (int i = 0; i < discardThresholds.size(); i++) {
            discardThresholdsBoxContents[i] = discardThresholds.get(i);
        }
        leaderboard.name = entryField.getValue();
        leaderboard.discardThresholds = discardThresholdsBoxContents;
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(entryField);
        panel.add(new Label(stringConstants.discardRacesFromHowManyStartedRacesOn()));
        HorizontalPanel hp = new HorizontalPanel();
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            hp.add(new Label("" + (i + 1) + "."));
            hp.add(discardThresholdBoxes[i]);
        }
        panel.add(hp);
        return panel;
    }

    @Override
    public void show() {
        super.show();
        entryField.setFocus(true);
    }
    
}
