package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventEditDialog extends EventDialog {

    public EventEditDialog(EventDTO event, Collection<EventDTO> otherExistingEvents,
            StringMessages stringConstants, AsyncCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringConstants, otherExistingEvents), stringConstants, callback);

        nameEntryField = createTextBox(event.name);
        nameEntryField.setWidth("200px");
        venueEntryField = createTextBox(event.venue.name);
        venueEntryField.setWidth("200px");
        publicationUrlEntryField = createTextBox(event.publicationUrl);
        publicationUrlEntryField.setWidth("200px");
    }
}
