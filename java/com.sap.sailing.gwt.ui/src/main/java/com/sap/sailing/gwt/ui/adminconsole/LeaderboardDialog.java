package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;


public abstract class LeaderboardDialog extends DataEntryDialog<StrippedLeaderboardDTO> {
    protected final StringMessages stringConstants;
    protected TextBox entryField;
    protected StrippedLeaderboardDTO leaderboard;
    protected ListBox regattaListBox;
    protected Collection<RegattaDTO> existingRegattas;
    
    protected LongBox[] discardThresholdBoxes;
    protected static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

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
                            && leaderboardToValidate.discardThresholds[i - 1] < leaderboardToValidate.discardThresholds[i];
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
    
    public LeaderboardDialog(StrippedLeaderboardDTO leaderboardDTO, Collection<RegattaDTO> existingRegattas, StringMessages stringConstants,
            ErrorReporter errorReporter, LeaderboardParameterValidator validator,  AsyncCallback<StrippedLeaderboardDTO> callback) {
        super(stringConstants.leaderboardName(), null, stringConstants.ok(),
                stringConstants.cancel(), validator, callback);
        this.existingRegattas = existingRegattas;
        this.stringConstants = stringConstants;
        this.leaderboard = leaderboardDTO;
    }
    
    @Override
    protected StrippedLeaderboardDTO getResult() {
        List<Integer> discardThresholds = new ArrayList<Integer>();
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            if (discardThresholdBoxes[i].getValue() != null
                    && discardThresholdBoxes[i].getValue().toString().length() > 0) {
                discardThresholds.add(discardThresholdBoxes[i].getValue().intValue());
            }
        }
        int[] discardThresholdsBoxContents = new int[discardThresholds.size()];
        for (int i = 0; i < discardThresholds.size(); i++) {
            discardThresholdsBoxContents[i] = discardThresholds.get(i);
        }
        leaderboard.name = entryField.getValue();
        leaderboard.discardThresholds = discardThresholdsBoxContents;
        leaderboard.regatta = getSelectedRegatta();
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
        
        HorizontalPanel regattaPanel = new HorizontalPanel();
        regattaPanel.setSpacing(3);
        regattaPanel.add(new Label(stringConstants.regatta() + ":"));
        regattaListBox.addItem(stringConstants.noRegatta());
        for (RegattaDTO regatta : existingRegattas) {
            regattaListBox.addItem(regatta.name);
        }
        regattaPanel.add(regattaListBox);
        panel.add(regattaPanel);
        
        panel.add(new Label(stringConstants.discardRacesFromHowManyStartedRacesOn()));
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(3);
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            hp.add(new Label("" + (i + 1) + "."));
            hp.add(discardThresholdBoxes[i]);
        }
        panel.add(hp);
        return panel;
    }

    public RegattaDTO getSelectedRegatta() {
        RegattaDTO result = null;
        int selIndex = regattaListBox.getSelectedIndex();
        if(selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = regattaListBox.getItemText(selIndex);
            for(RegattaDTO regattaDTO: existingRegattas) {
                if(regattaDTO.name.equals(itemText)) {
                    result = regattaDTO;
                    break;
                }
            }
        }
        return result;
    }
    
    @Override
    public void show() {
        super.show();
        entryField.setFocus(true);
    }
    
}
