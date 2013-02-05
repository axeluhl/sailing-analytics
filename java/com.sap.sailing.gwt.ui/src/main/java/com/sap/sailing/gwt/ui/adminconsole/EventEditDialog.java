package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventEditDialog extends EventDialog {

    public EventEditDialog(EventDTO event, Collection<EventDTO> otherExistingEvents,
            StringMessages stringConstants, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringConstants, otherExistingEvents), stringConstants, callback);

        nameEntryField = createTextBox(event.name);
        nameEntryField.setWidth("200px");
        venueEntryField = createTextBox(event.venue.name);
        venueEntryField.setWidth("200px");
        publicationUrlEntryField = createTextBox(event.publicationUrl);
        publicationUrlEntryField.setWidth("200px");
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(event.isPublic);
        id = event.id;
        
        if (event.venue.getCourseAreas() != null && event.venue.getCourseAreas().size() > 0) {
        	for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
        		addCourseAreaWidget(courseArea.name, false);
        	}
        	
        }
    }
}
