package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class EventCreateDialog extends EventDialog {

    public EventCreateDialog(Collection<EventDTO> existingEvents, List<LeaderboardGroupDTO> availableLeaderboardGroups, 
            SailingServiceAsync sailingService, StringMessages stringMessages, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringMessages, existingEvents), sailingService, stringMessages,
                availableLeaderboardGroups, /* leaderboardGroups */ Collections.<LeaderboardGroupDTO>emptyList(), callback);
        nameEntryField = createTextBox(null);
        nameEntryField.setVisibleLength(50);
        nameEntryField.ensureDebugId("NameTextBox");
        descriptionEntryField = createTextArea(null);
        descriptionEntryField.setCharacterWidth(50);
        descriptionEntryField.setVisibleLines(2);
        descriptionEntryField.getElement().getStyle().setProperty("resize", "none");
        descriptionEntryField.ensureDebugId("DescriptionTextArea");
        venueEntryField = createTextBox(null);
        venueEntryField.setVisibleLength(35);
        venueEntryField.ensureDebugId("VenueTextBox");
        final Date now = new Date();
        startDateBox = createDateTimeBox(now);
        startDateBox.setFormat("dd/mm/yyyy hh:ii");
        startDateBox.ensureDebugId("StartDateTimeBox");
        endDateBox = createDateTimeBox(now);
        endDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        endDateBox.ensureDebugId("EndDateTimeBox");
        officialWebsiteURLEntryField = createTextBox(null);
        officialWebsiteURLEntryField.setVisibleLength(50);
        baseURLEntryField = createTextBox(null);
        baseURLEntryField.setVisibleLength(50);
        sailorsInfoWebsiteURLEntryFields = createTextBoxesForLocalesAndDefault(Collections.<String, String> emptyMap());
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(false);
        isPublicCheckBox.ensureDebugId("IsPublicCheckBox");
        imagesListComposite.fillImages(Collections.<ImageDTO>emptyList());
        videosListComposite.fillVideos(Collections.<VideoDTO>emptyList());
        // add default course area
        courseAreaNameList.setValue(Collections.singletonList(new CourseAreaDTO("Default")));
    }
}
