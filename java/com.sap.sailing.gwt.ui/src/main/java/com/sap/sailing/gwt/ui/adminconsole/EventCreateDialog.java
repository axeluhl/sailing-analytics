package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventCreateDialog extends EventDialog {

    public EventCreateDialog(Collection<EventDTO> existingEvents,
            StringMessages stringConstants, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringConstants, existingEvents),
                stringConstants, callback);

        nameEntryField = createTextBox(null);
        nameEntryField.setWidth("200px");
        venueEntryField = createTextBox(null);
        venueEntryField.setWidth("200px");
        publicationUrlEntryField = createTextBox(null);
        publicationUrlEntryField.setWidth("200px");
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(false);
    }
}
