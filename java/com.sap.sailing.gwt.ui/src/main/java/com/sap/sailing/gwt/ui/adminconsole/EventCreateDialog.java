package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventCreateDialog extends EventDialog {

    public EventCreateDialog(Collection<EventDTO> existingEvents, StringMessages stringConstants, DialogCallback<EventDTO> callback) {
        super(new EventParameterValidator(stringConstants, existingEvents), stringConstants, callback);

        nameEntryField = createTextBox(null);
        nameEntryField.setVisibleLength(50);
        venueEntryField = createTextBox(null);
        venueEntryField.setVisibleLength(35);
        startDateBox = createDateBox(12);
        startDateBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT))); 
        endDateBox = createDateBox(12);
        endDateBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT))); 
        isPublicCheckBox = createCheckbox("");
        isPublicCheckBox.setValue(false);
    }
}
