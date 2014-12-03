package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sse.common.Util;

public class EventEditDialog extends EventDialog {
    public EventEditDialog(EventDTO event, Collection<EventDTO> otherExistingEvents, List<LeaderboardGroupDTO> availableLeaderboardGroups, StringMessages stringMessages, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringMessages, otherExistingEvents), stringMessages, availableLeaderboardGroups, event.getLeaderboardGroups(), callback);
        nameEntryField = createTextBox(event.getName());
        nameEntryField.setVisibleLength(50);
        descriptionEntryField = createTextArea(event.getDescription());
        descriptionEntryField.setCharacterWidth(50);
        descriptionEntryField.setVisibleLines(2);
        descriptionEntryField.getElement().getStyle().setProperty("resize", "none");
        venueEntryField = createTextBox(event.venue.getName());
        venueEntryField.setVisibleLength(35);
        startDateBox = createDateTimeBox(event.startDate);
        startDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        endDateBox = createDateTimeBox(event.endDate);
        endDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(event.isPublic);
        id = event.id;

        List<String> courseAreaNames = new ArrayList<>();
        if (event.venue.getCourseAreas() != null && event.venue.getCourseAreas().size() > 0) {
            for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                courseAreaNames.add(courseArea.getName());
            }
        }
        officialWebsiteURLEntryField = createTextBox(event.getOfficialWebsiteURL());
        officialWebsiteURLEntryField.setVisibleLength(50);
        logoImageURLEntryField = createTextBox(event.getLogoImageURL());
        logoImageURLEntryField.setVisibleLength(50);
        courseAreaNameList.setValue(courseAreaNames);
        List<String> imageURLStringsAsList = new ArrayList<>();
        Util.addAll(event.getImageURLs(), imageURLStringsAsList);
        imageURLList.setValue(imageURLStringsAsList);
        List<String> videoURLStringsAsList = new ArrayList<>();
        Util.addAll(event.getVideoURLs(), videoURLStringsAsList);
        videoURLList.setValue(videoURLStringsAsList);
        List<String> sponsorImageURLStringsAsList = new ArrayList<>();
        Util.addAll(event.getSponsorImageURLs(), sponsorImageURLStringsAsList);
        sponsorImageURLList.setValue(sponsorImageURLStringsAsList);
        List<String> leaderboardGroupNames = new ArrayList<>();
        for(LeaderboardGroupDTO leaderboardGroupDTO: event.getLeaderboardGroups()) {
            leaderboardGroupNames.add(leaderboardGroupDTO.getName());
        }
        leaderboardGroupList.setValue(leaderboardGroupNames);
    }
}
