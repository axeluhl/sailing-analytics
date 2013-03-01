package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class FlexibleLeaderboardEditDialog extends FlexibleLeaderboardDialog {
    
    public FlexibleLeaderboardEditDialog(Collection<StrippedLeaderboardDTO> otherExistingLeaderboards,
            LeaderboardDescriptor leaderboard, StringMessages stringConstants, List<EventDTO> existingEvents, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptor> callback) {
        super(stringConstants.editFlexibleLeaderboard(), leaderboard, stringConstants, existingEvents, errorReporter, new FlexibleLeaderboardDialog.LeaderboardParameterValidator(
                stringConstants, otherExistingLeaderboards), callback);
        
        nameTextBox = createTextBox(leaderboard.getName());
        displayNameTextBox = createTextBox(leaderboard.getDisplayName()); 
        scoringSchemeListBox = createListBox(false);
        int j = 0;
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringConstants));
            if(leaderboard.getScoringScheme() == scoringSchemeType) {
                scoringSchemeListBox.setSelectedIndex(j);
            }
            j++;
        }
        scoringSchemeListBox.setEnabled(false);
        
        sailingEventsListBox = createSailingEventListBox();
        for (EventDTO event : existingEvents) {
            for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                if (courseArea.id.equals(leaderboard.getCourseAreaIdAsString())) {
                    int index = existingEvents.indexOf(event) + 1; // + 1 because of the "Please select... item"
                    sailingEventsListBox.setSelectedIndex(index);
                    onEventSelectionChanged();
                    int courseAreaIndex = event.venue.getCourseAreas().indexOf(courseArea) + 1; // + 1 because of the "Please select... item"
                    courseAreaListBox.setSelectedIndex(courseAreaIndex);
                    break;
                }
            }
        }
        discardThresholdBoxes = initPrefilledDiscardThresholdBoxes(leaderboard.getDiscardThresholds(), this);
    }
}
