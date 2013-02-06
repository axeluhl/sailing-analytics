package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;


public abstract class RegattaLeaderboardDialog extends AbstractLeaderboardDialog {
    protected ListBox regattaListBox;
    protected Collection<RegattaDTO> existingRegattas;

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
            
            boolean regattaSelected = leaderboardToValidate.getRegattaName() != null ? true : false;
            
            if (!nonEmpty) {
                errorMessage = stringConstants.pleaseEnterAName();
            } else if(!regattaSelected){
                errorMessage = stringConstants.pleaseSelectARegatta();
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
    
    public RegattaLeaderboardDialog(String title, LeaderboardDescriptor leaderboardDTO, Collection<RegattaDTO> existingRegattas, StringMessages stringConstants,
    		Collection<EventDTO> existingEvents, ErrorReporter errorReporter, LeaderboardParameterValidator validator,  DialogCallback<LeaderboardDescriptor> callback) {
        super(title, leaderboardDTO, stringConstants, existingEvents, validator, callback);
        this.existingRegattas = existingRegattas;
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
        LeaderboardDescriptor leaderboard = super.getResult();
        leaderboard.setRegattaName(getSelectedRegatta() != null ? getSelectedRegatta().name : null);
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        
        Grid formGrid = new Grid(5,2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0,  0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameTextBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.regatta() + ":"));
        formGrid.setWidget(1, 1, regattaListBox);
        formGrid.setWidget(2, 0, new Label(stringMessages.event() + ":"));
        formGrid.setWidget(2, 1, sailingEventsListBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.courseArea() + ":"));
        formGrid.setWidget(3, 1, courseAreaListBox);
                
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

}
