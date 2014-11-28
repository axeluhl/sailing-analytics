package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public class EventCreateDialog extends EventDialog {

    public EventCreateDialog(Collection<EventDTO> existingEvents, List<LeaderboardGroupDTO> availableLeaderboardGroups, StringMessages stringConstants, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringConstants, existingEvents), stringConstants,
                availableLeaderboardGroups, /* leaderboardGroups */ Collections.<LeaderboardGroupDTO>emptyList(), callback);
        nameEntryField = createTextBox(null);
        nameEntryField.setVisibleLength(50);
        descriptionEntryField = createTextArea(null);
        descriptionEntryField.setCharacterWidth(50);
        descriptionEntryField.setVisibleLines(2);
        descriptionEntryField.getElement().getStyle().setProperty("resize", "none");
        venueEntryField = createTextBox(null);
        venueEntryField.setVisibleLength(35);
        final Date now = new Date();
        startDateBox = createDateTimeBox(now);
        startDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        endDateBox = createDateTimeBox(now);
        endDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        officialWebsiteURLEntryField = createTextBox(null);
        officialWebsiteURLEntryField.setVisibleLength(50);
        logoImageURLEntryField = createTextBox(null);
        logoImageURLEntryField.setVisibleLength(50);
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(false);
    }
}
