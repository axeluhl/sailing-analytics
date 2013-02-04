package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;

public class EventDialog extends DataEntryDialog<EventDTO> {
    protected StringMessages stringConstants;
    protected TextBox nameEntryField;
    protected TextBox venueEntryField;
    protected TextBox publicationUrlEntryField;
    protected CheckBox isPublicCheckBox;
    protected String id;
    protected List<TextBox> courseAreaNameEntryFields;
    
    private EventDTO event;
    
    private Grid courseAreasGrid;

    protected static class EventParameterValidator implements Validator<EventDTO> {

        private StringMessages stringConstants;
        private ArrayList<EventDTO> existingEvents;

        public EventParameterValidator(StringMessages stringConstants, Collection<EventDTO> existingEvents) {
            this.stringConstants = stringConstants;
            this.existingEvents = new ArrayList<EventDTO>(existingEvents);
        }

        @Override
        public String getErrorMessage(EventDTO eventToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = eventToValidate.name != null && eventToValidate.name.length() > 0;
            boolean venueNotEmpty = eventToValidate.venue.name != null && eventToValidate.venue.name.length() > 0;
            boolean courseAreaNotEmpty = eventToValidate.venue.getCourseAreas() != null && eventToValidate.venue.getCourseAreas().size() > 0;
            
            if (courseAreaNotEmpty) {
            	for (CourseAreaDTO courseArea : eventToValidate.venue.getCourseAreas()) {
            		courseAreaNotEmpty = courseArea.name != null && courseArea.name.length() > 0;
            		if (!courseAreaNotEmpty)
            			break;
            	}
            }

            boolean unique = true;
            for (EventDTO event : existingEvents) {
                if (event.name.equals(eventToValidate.name)) {
                    unique = false;
                    break;
                }
            }

            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterAName();
            } else if (!venueNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyVenue();
            } else if (!courseAreaNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyCourseArea();
            } else if (!unique) {
                errorMessage = stringConstants.eventWithThisNameAlreadyExists();
            }

            return errorMessage;
        }

    }

    public EventDialog(EventParameterValidator validator, StringMessages stringConstants,
            DialogCallback<EventDTO> callback) {
        super(stringConstants.event(), null, stringConstants.ok(), stringConstants.cancel(), validator,
                callback);
        this.event = new EventDTO();
        this.stringConstants = stringConstants;
        courseAreaNameEntryFields = new ArrayList<TextBox>();
        courseAreasGrid = new Grid(0, 0);
    }
    
    protected void addCourseAreaWidget(String courseAreaName) {
        createCourseAreaNameWidget(courseAreaName);
    }
    
    private Widget createCourseAreaNameWidget(String defaultName) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setVisibleLength(40);
        textBox.setWidth("175px");
        courseAreaNameEntryFields.add(textBox);
        return textBox; 
    }

    @Override
    protected EventDTO getResult() {
    	this.event.name = nameEntryField.getText();
    	this.event.publicationUrl = publicationUrlEntryField.getText();
    	this.event.isPublic = isPublicCheckBox.getValue();
    	this.event.id = id;
    	
    	List<CourseAreaDTO> courseAreas = new ArrayList<CourseAreaDTO>();
    	int count = courseAreaNameEntryFields.size();
    	for(int i = 0; i < count; i++) {
    		CourseAreaDTO courseAreaDTO = new CourseAreaDTO();
    		courseAreaDTO.name = courseAreaNameEntryFields.get(i).getValue();
    		courseAreas.add(courseAreaDTO);
    	}
    	
    	this.event.venue = new VenueDTO(venueEntryField.getText(), courseAreas);
    	
        return this.event;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        
        Grid formGrid = new Grid(4, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0,  0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringConstants.venue() + ":"));
        formGrid.setWidget(1, 1, venueEntryField);
        formGrid.setWidget(2, 0, new Label(stringConstants.publicationUrl() + ":"));
        formGrid.setWidget(2, 1, publicationUrlEntryField);
        formGrid.setWidget(3, 0, new Label(stringConstants.isPublic() + ":"));
        formGrid.setWidget(3, 1, isPublicCheckBox);
        
        panel.add(createHeadlineLabel(stringConstants.courseAreas()));
        panel.add(courseAreasGrid);
        updateCourseAreasGrid(panel);
        
        Button addCourseAreaButton = new Button(stringConstants.addCourseArea());
        addCourseAreaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addCourseAreaWidget("");
                updateCourseAreasGrid(panel);
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
        courseAreasGrid.setHTML(0, 0, stringConstants.name());
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
