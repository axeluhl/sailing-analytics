package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class EventDialog extends DataEntryDialog<EventDTO> {
    protected StringMessages stringMessages;
    protected TextBox nameEntryField;
    protected TextBox venueEntryField;
    protected DateBox startDateBox;
    protected DateBox endDateBox;
    protected CheckBox isPublicCheckBox;
    protected UUID id;
    protected List<TextBox> courseAreaNameEntryFields;

    private Grid courseAreasGrid;

    protected static class EventParameterValidator implements Validator<EventDTO> {

        private StringMessages stringMessages;
        private ArrayList<EventDTO> existingEvents;

        public EventParameterValidator(StringMessages stringMessages, Collection<EventDTO> existingEvents) {
            this.stringMessages = stringMessages;
            this.existingEvents = new ArrayList<EventDTO>(existingEvents);
        }

        @Override
        public String getErrorMessage(EventDTO eventToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = eventToValidate.getName() != null && eventToValidate.getName().length() > 0;
            boolean venueNotEmpty = eventToValidate.venue.getName() != null && eventToValidate.venue.getName().length() > 0;
            boolean courseAreaNotEmpty = eventToValidate.venue.getCourseAreas() != null && eventToValidate.venue.getCourseAreas().size() > 0;

            if (courseAreaNotEmpty) {
                for (CourseAreaDTO courseArea : eventToValidate.venue.getCourseAreas()) {
                    courseAreaNotEmpty = courseArea.getName() != null && courseArea.getName().length() > 0;
                    if (!courseAreaNotEmpty)
                        break;
                }
            }

            boolean unique = true;
            for (EventDTO event : existingEvents) {
                if (event.getName().equals(eventToValidate.getName())) {
                    unique = false;
                    break;
                }
            }

            Date startDate = eventToValidate.startDate;
            Date endDate = eventToValidate.endDate;
            String datesErrorMessage = null;
            // remark: startDate == null and endDate == null is valid
            if(startDate != null && endDate != null) {
                if(startDate.after(endDate)) {
                    datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate(); 
                }
            } else if((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate();
            }
            
            if(datesErrorMessage != null) {
                errorMessage = datesErrorMessage;
            } else if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (!venueNotEmpty) {
                errorMessage = stringMessages.pleaseEnterNonEmptyVenue();
            } else if (!courseAreaNotEmpty) {
                errorMessage = stringMessages.pleaseEnterNonEmptyCourseArea();
            } else if (!unique) {
                errorMessage = stringMessages.eventWithThisNameAlreadyExists();
            }

            return errorMessage;
        }

    }

    public EventDialog(EventParameterValidator validator, StringMessages stringConstants,
            DialogCallback<EventDTO> callback) {
        super(stringConstants.event(), null, stringConstants.ok(), stringConstants.cancel(), validator,
                callback);
        this.stringMessages = stringConstants;
        courseAreaNameEntryFields = new ArrayList<TextBox>();
        courseAreasGrid = new Grid(0, 0);
    }

    protected void addCourseAreaWidget(String courseAreaName, boolean isEnabled) {
        createCourseAreaNameWidget(courseAreaName, isEnabled);
    }

    private Widget createCourseAreaNameWidget(String defaultName, boolean isEnabled) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setVisibleLength(40);
        textBox.setEnabled(isEnabled);
        textBox.setWidth("175px");
        courseAreaNameEntryFields.add(textBox);
        return textBox; 
    }

    @Override
    protected EventDTO getResult() {
        EventDTO result = new EventDTO();
        
        result.setName(nameEntryField.getText());
        result.startDate = startDateBox.getValue();
        result.endDate = endDateBox.getValue();
        result.isPublic = isPublicCheckBox.getValue();
        result.id = id;

        List<CourseAreaDTO> courseAreas = new ArrayList<CourseAreaDTO>();
        int count = courseAreaNameEntryFields.size();
        for(int i = 0; i < count; i++) {
            CourseAreaDTO courseAreaDTO = new CourseAreaDTO();
            courseAreaDTO.setName(courseAreaNameEntryFields.get(i).getValue());
            courseAreas.add(courseAreaDTO);
        }

        result.venue = new VenueDTO(venueEntryField.getText(), courseAreas);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        Grid formGrid = new Grid(5, 2);
        panel.add(formGrid);

        formGrid.setWidget(0,  0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringMessages.venue() + ":"));
        formGrid.setWidget(1, 1, venueEntryField);
        formGrid.setWidget(2, 0, new Label(stringMessages.startDate() + ":"));
        formGrid.setWidget(2, 1, startDateBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.endDate() + ":"));
        formGrid.setWidget(3, 1, endDateBox);
        formGrid.setWidget(4, 0, new Label(stringMessages.isPublic() + ":"));
        formGrid.setWidget(4, 1, isPublicCheckBox);

        panel.add(createHeadlineLabel(stringMessages.courseAreas()));
        panel.add(courseAreasGrid);
        updateCourseAreasGrid(panel);

        Button addCourseAreaButton = new Button(stringMessages.addCourseArea());
        addCourseAreaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addCourseAreaWidget("", true);
                updateCourseAreasGrid(panel);
                courseAreaNameEntryFields.get(courseAreaNameEntryFields.size()-1).setFocus(true);
            }
        });
        panel.add(addCourseAreaButton);
        return panel;
    }

    protected void updateCourseAreasGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(courseAreasGrid);
        parentPanel.remove(courseAreasGrid);
        int courseAreasCount = courseAreaNameEntryFields.size();
        courseAreasGrid = new Grid(courseAreasCount + 1, 1);
        courseAreasGrid.setCellSpacing(4);
        courseAreasGrid.setHTML(0, 0, stringMessages.name());
        for(int i = 0; i < courseAreasCount; i++) {
            courseAreasGrid.setWidget(i+1, 0, courseAreaNameEntryFields.get(i));
        }
        parentPanel.insert(courseAreasGrid, widgetIndex);
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
