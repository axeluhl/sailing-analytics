package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;

public class EventDialog extends DataEntryDialog<EventDTO> {
    protected StringMessages stringConstants;
    protected TextBox nameEntryField;
    protected TextBox venueEntryField;
    protected TextBox publicationUrlEntryField;
    protected CheckBox isPublicCheckBox;

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
        this.stringConstants = stringConstants;
    }

    @Override
    protected EventDTO getResult() {
        EventDTO eventDTO = new EventDTO(nameEntryField.getText());
        eventDTO.venue = new VenueDTO(venueEntryField.getText());
        eventDTO.publicationUrl = publicationUrlEntryField.getText();
        eventDTO.isPublic = isPublicCheckBox.getValue();
        return eventDTO;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
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
        return panel;
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
