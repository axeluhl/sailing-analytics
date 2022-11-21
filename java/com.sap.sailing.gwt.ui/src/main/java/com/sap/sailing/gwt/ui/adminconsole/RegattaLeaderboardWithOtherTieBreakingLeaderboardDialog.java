package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaLeaderboardWithOtherTieBreakingLeaderboardDialog extends RegattaLeaderboardDialog<LeaderboardDescriptorWithOtherTieBreakingLeaderboard> {
    protected ListBox otherTieBreakingLeaderboardsListBox;

    public RegattaLeaderboardWithOtherTieBreakingLeaderboardDialog(
            String title, LeaderboardDescriptorWithOtherTieBreakingLeaderboard leaderboardDescriptor,
            Collection<StrippedLeaderboardDTO> existingLeaderboards, Collection<RegattaDTO> existingRegattas,
            StringMessages stringMessages, ErrorReporter errorReporter, DialogCallback<LeaderboardDescriptorWithOtherTieBreakingLeaderboard> callback) {
        super(title, leaderboardDescriptor,
                existingRegattas, stringMessages, errorReporter,
                new RegattaLeaderboardDialog.LeaderboardParameterValidator<LeaderboardDescriptorWithOtherTieBreakingLeaderboard>(
                        stringMessages, existingLeaderboards),
                callback);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(4, 3);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, createLabel(stringMessages.regatta()));
        formGrid.setWidget(0, 1, regattaListBox);
        formGrid.setWidget(1, 0, createLabel(stringMessages.otherTieBreakingLeaderboard()));
        formGrid.setWidget(1, 1, otherTieBreakingLeaderboardsListBox);
        formGrid.setWidget(2, 0, createLabel(stringMessages.name()));
        formGrid.setWidget(2, 1, nameTextBox);
        formGrid.setWidget(3, 0, createLabel(stringMessages.displayName()));
        formGrid.setWidget(3, 1, displayNameTextBox);
        mainPanel.add(formGrid);
        mainPanel.add(regattaDefinesDiscardsLabel);
        if (discardThresholdBoxes != null) {
            mainPanel.add(discardThresholdBoxes.getWidget());
            regattaDefinesDiscardsLabel.setVisible(false);
        }
        return mainPanel;
    }

    @Override
    protected LeaderboardDescriptorWithOtherTieBreakingLeaderboard getResult() {
        final LeaderboardDescriptor interimsResult = super.getResult();
        return new LeaderboardDescriptorWithOtherTieBreakingLeaderboard(interimsResult.getName(),
                interimsResult.getDisplayName(), interimsResult.getScoringScheme(),
                interimsResult.getDiscardThresholds(), interimsResult.getRegattaName(),
                interimsResult.getCourseAreaIds(), otherTieBreakingLeaderboardsListBox.getSelectedValue());
    }
}
