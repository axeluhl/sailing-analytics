package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventEditDialog extends EventDialog {

    public EventEditDialog(EventDTO event, Collection<EventDTO> otherExistingEvents, StringMessages stringConstants, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringConstants, otherExistingEvents), stringConstants, callback);

        nameEntryField = createTextBox(event.getName());
        nameEntryField.setVisibleLength(50);
        venueEntryField = createTextBox(event.venue.getName());
        venueEntryField.setVisibleLength(35);
        startDateBox = createDateBox(event.startDate, 12);
        startDateBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT))); 
        endDateBox = createDateBox(event.endDate, 12);
        endDateBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT))); 
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(event.isPublic);
        id = event.id;

        if (event.venue.getCourseAreas() != null && event.venue.getCourseAreas().size() > 0) {
            for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                addCourseAreaWidget(courseArea.getName(), false);
            }

        }
    }
}
