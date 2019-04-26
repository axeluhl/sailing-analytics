package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;


public abstract class RegattaLeaderboardDialog extends AbstractLeaderboardDialog<LeaderboardDescriptor> {
    protected ListBox regattaListBox;
    protected Collection<RegattaDTO> existingRegattas;
    private Label regattaDefinesDiscardsLabel;

    protected static class LeaderboardParameterValidator implements Validator<LeaderboardDescriptor> {
        protected final StringMessages stringMessages;
        protected final Collection<StrippedLeaderboardDTO> existingLeaderboards;

        public LeaderboardParameterValidator(StringMessages stringMessages, Collection<StrippedLeaderboardDTO> existingLeaderboards){
            super();
            this.stringMessages = stringMessages;
            this.existingLeaderboards = existingLeaderboards;
        }

        @Override
        public String getErrorMessage(LeaderboardDescriptor leaderboardToValidate) {
            String errorMessage;
            boolean unique = true;
            for (StrippedLeaderboardDTO dao : existingLeaderboards) {
                if (dao.getName().equals(leaderboardToValidate.getName())) {
                    unique = false;
                }
            }
            boolean regattaSelected = leaderboardToValidate.getRegattaName() != null ? true : false;
            if (!regattaSelected) {
                errorMessage = stringMessages.pleaseSelectARegatta();
            } else if (!unique) {
                errorMessage = stringMessages.leaderboardWithThisNameAlreadyExists();
            } else {
                String discardThresholdErrorMessage = DiscardThresholdBoxes
                        .getErrorMessage(leaderboardToValidate.getDiscardThresholds(), stringMessages);
                if (discardThresholdErrorMessage != null) {
                    errorMessage = discardThresholdErrorMessage;
                } else {
                    errorMessage = null;
                }
            }
            return errorMessage;
        }
    }

    public RegattaLeaderboardDialog(String title, LeaderboardDescriptor leaderboardDTO, Collection<RegattaDTO> existingRegattas, StringMessages stringMessages,
            ErrorReporter errorReporter, LeaderboardParameterValidator validator,  DialogCallback<LeaderboardDescriptor> callback) {
        super(title, leaderboardDTO, stringMessages, validator, callback);
        this.existingRegattas = existingRegattas;
        regattaDefinesDiscardsLabel = new Label(stringMessages.regattaDefinesResultDiscardingRules());
    }

    protected void adjustVisibilityOfResultDiscardingRuleComponent() {
        if (getSelectedRegatta().definesSeriesDiscardThresholds()) {
            if (discardThresholdBoxes != null) {
                discardThresholdBoxes.getWidget().setVisible(false);
            }
            regattaDefinesDiscardsLabel.setVisible(true);
        } else {
            if (discardThresholdBoxes != null) {
                discardThresholdBoxes.getWidget().setVisible(true);
            }
            regattaDefinesDiscardsLabel.setVisible(false);
        }
    }

    protected ListBox createSortedRegattaListBox(Collection<RegattaDTO> regattas, String preSelectedRegattaName) {
        ListBox result = createListBox(false);

        // sort the regatta names
        List<String> sortedRegattaNames = new ArrayList<String>();
        for (RegattaDTO regatta : existingRegattas) {
            sortedRegattaNames.add(regatta.getName());
        }
        Collections.sort(sortedRegattaNames);
        
        result.addItem(stringMessages.pleaseSelectARegatta());
        int i=1;
        for (String regattaName : sortedRegattaNames) {
            result.addItem(regattaName);
            if (preSelectedRegattaName != null && regattaName.equals(preSelectedRegattaName)) {
                result.setSelectedIndex(i);
            }
            i++;
        }
        return result;
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
        LeaderboardDescriptor leaderboard = super.getResult();
        leaderboard.setRegattaName(getSelectedRegatta() != null ? getSelectedRegatta().getName() : null);
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(3,3);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, createLabel(stringMessages.regatta()));
        formGrid.setWidget(0, 1, regattaListBox);
        formGrid.setWidget(1,  0, createLabel(stringMessages.name()));
        formGrid.setWidget(1, 1, nameTextBox);
        formGrid.setWidget(2,  0, createLabel(stringMessages.displayName()));
        formGrid.setWidget(2, 1, displayNameTextBox);
        mainPanel.add(formGrid);
        mainPanel.add(regattaDefinesDiscardsLabel);
        if (discardThresholdBoxes != null) {
            mainPanel.add(discardThresholdBoxes.getWidget());
            regattaDefinesDiscardsLabel.setVisible(false);
        }
        return mainPanel;
    }

    public RegattaDTO getSelectedRegatta() {
        RegattaDTO result = null;
        int selIndex = regattaListBox.getSelectedIndex();
        if (selIndex > 0) { // the zero index represents the 'no selection' text
            String itemValue = regattaListBox.getValue(selIndex);
            for (RegattaDTO regattaDTO : existingRegattas) {
                if (regattaDTO.getName().equals(itemValue)) {
                    result = regattaDTO;
                    break;
                }
            }
        }
        return result;
    }

}
