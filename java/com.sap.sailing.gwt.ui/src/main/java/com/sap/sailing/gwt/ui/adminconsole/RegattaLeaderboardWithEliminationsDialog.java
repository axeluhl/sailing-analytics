package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;


public abstract class RegattaLeaderboardWithEliminationsDialog extends AbstractLeaderboardDialog {
    protected ListBox regattaLeaderboardsListBox;
    protected Collection<RegattaDTO> existingRegattas;

    protected static class LeaderboardParameterValidator implements Validator<LeaderboardDescriptor> {
        protected final StringMessages stringMessages;
        protected final Collection<StrippedLeaderboardDTO> existingLeaderboards;

        public LeaderboardParameterValidator(StringMessages stringMessages, Collection<StrippedLeaderboardDTO> existingLeaderboards) {
            super();
            this.stringMessages = stringMessages;
            this.existingLeaderboards = existingLeaderboards;
        }

        @Override
        public String getErrorMessage(LeaderboardDescriptor leaderboardToValidate) {
            String errorMessage;
            boolean unique = true;
            for (StrippedLeaderboardDTO dao : existingLeaderboards) {
                if (dao.name.equals(leaderboardToValidate.getName())) {
                    unique = false;
                }
            }
            boolean regattaSelected = leaderboardToValidate.getRegattaName() != null ? true : false;
            if (!regattaSelected) {
                errorMessage = stringMessages.pleaseSelectARegatta();
            } else if (!unique) {
                errorMessage = stringMessages.leaderboardWithThisNameAlreadyExists();
            } else {
                String discardThresholdErrorMessage = DiscardThresholdBoxes.getErrorMessage(
                        leaderboardToValidate.getDiscardThresholds(), stringMessages);
                if (discardThresholdErrorMessage != null) {
                    errorMessage = discardThresholdErrorMessage;
                } else {
                    errorMessage = null;
                }
            }
            return errorMessage;
        }
    }

    public RegattaLeaderboardWithEliminationsDialog(String title, LeaderboardDescriptor leaderboardDTO,
            Collection<RegattaDTO> existingRegattas, StringMessages stringMessages, ErrorReporter errorReporter,
            LeaderboardParameterValidator validator, DialogCallback<LeaderboardDescriptor> callback) {
        super(title, leaderboardDTO, stringMessages, validator, callback);
        this.existingRegattas = existingRegattas;
    }

    protected ListBox createSortedRegattaLeaderboardsListBox(Collection<StrippedLeaderboardDTO> existingLeaderboards, String preSelectedRegattaName) {
        ListBox result = createListBox(false);
        // sort the regatta names
        List<StrippedLeaderboardDTO> sortedRegattaLeaderboards = new ArrayList<>();
        for (StrippedLeaderboardDTO leaderboard : existingLeaderboards) {
            sortedRegattaLeaderboards.add(leaderboard);
        }
        Collections.sort(sortedRegattaLeaderboards, (rl1, rl2) -> rl1.name.compareTo(rl2.name));
        result.addItem(stringMessages.pleaseSelectARegatta());
        int i=1;
        for (StrippedLeaderboardDTO leaderboard : sortedRegattaLeaderboards) {
            if (leaderboard.type == LeaderboardType.RegattaLeaderboard) {
                result.addItem(leaderboard.name, leaderboard.name);
                if (preSelectedRegattaName != null && leaderboard.name.equals(preSelectedRegattaName)) {
                    result.setSelectedIndex(i);
                }
                i++;
            }
        }
        return result;
    }
    
    @Override
    protected LeaderboardDescriptor getResult() {
        LeaderboardDescriptor leaderboard = super.getResult();
        leaderboard.setRegattaName(getNameOfSelectedRegattaLeaderboard());
        return leaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(3,3);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, createLabel(stringMessages.regattaLeaderboards()));
        formGrid.setWidget(0, 1, regattaLeaderboardsListBox);
        formGrid.setWidget(1,  0, createLabel(stringMessages.name()));
        formGrid.setWidget(1, 1, nameTextBox);
        formGrid.setWidget(2,  0, createLabel(stringMessages.displayName()));
        formGrid.setWidget(2, 1, displayNameTextBox);
        mainPanel.add(formGrid);
        return mainPanel;
    }

    public String getNameOfSelectedRegattaLeaderboard() {
        final String result;
        int selIndex = regattaLeaderboardsListBox.getSelectedIndex();
        if (selIndex > 0) { // the zero index represents the 'no selection' text
            result = regattaLeaderboardsListBox.getValue(selIndex);
        } else {
            result = null;
        }
        return result;
    }

}
