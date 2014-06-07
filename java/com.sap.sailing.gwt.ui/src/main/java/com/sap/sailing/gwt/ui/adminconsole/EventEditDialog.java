package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util;

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

        List<String> courseAreaNames = new ArrayList<>();
        if (event.venue.getCourseAreas() != null && event.venue.getCourseAreas().size() > 0) {
            for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                courseAreaNames.add(courseArea.getName());
            }
        }
        courseAreaNameList.setValue(courseAreaNames);
        List<String> imageURLStringsAsList = new ArrayList<>();
        Util.addAll(event.getImageURLs(), imageURLStringsAsList);
        imageURLList.setValue(imageURLStringsAsList);
        List<String> videoURLStringsAsList = new ArrayList<>();
        Util.addAll(event.getVideoURLs(), videoURLStringsAsList);
        videoURLList.setValue(videoURLStringsAsList);
    }
}
