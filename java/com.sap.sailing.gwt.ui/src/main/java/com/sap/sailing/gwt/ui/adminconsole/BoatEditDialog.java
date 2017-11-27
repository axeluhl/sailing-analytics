package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * @author Frank Mittag (c516374)
 * 
 */
public class BoatEditDialog extends DataEntryDialog<BoatDTO> {
    private final BoatDTO boatToEdit;
    private final TextBox name;
    private final SuggestBox boatClassName;
    private final TextBox displayColorTextBox;
    private final TextBox sailId;
    private final StringMessages stringMessages;
    
    /**
     * The class creates the UI-dialog to edit the data of a boat.
     * 
     * @param boatToEdit
     *            The 'boatToEdit' parameter contains the boat which should be changed or initialized.
     */
    public BoatEditDialog(final StringMessages stringMessages, BoatDTO boatToEdit,
            DialogCallback<BoatDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.boat(), null, stringMessages.ok(), stringMessages
                .cancel(), new Validator<BoatDTO>() {
                    @Override
                    public String getErrorMessage(BoatDTO valueToValidate) {
                        String result = null;
                        boolean invalidSailId = valueToValidate.getSailId() == null || valueToValidate.getSailId().isEmpty(); 
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        if (invalidSailId && invalidName) {
                            result = stringMessages.pleaseEnterASailNumberOrAName();
                        } else if (valueToValidate.getColor() != null && valueToValidate.getColor() instanceof InvalidColor) {
                            result = valueToValidate.getColor().getAsHtml();
                        } else if (valueToValidate.getBoatClass().getName() == null || valueToValidate.getBoatClass().getName().isEmpty()) {
                            result = stringMessages.pleaseEnterABoatClass();
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("BoatEditDialog");
        this.stringMessages = stringMessages;
        this.boatToEdit = boatToEdit;
                
        this.boatClassName = createSuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassName.ensureDebugId("BoatClassNameSuggestBox");
        if (boatToEdit.getBoatClass() != null) {
            boatClassName.setValue(boatToEdit.getBoatClass().getName());
            boatClassName.setEnabled(false);
        } else {
            boatClassName.setValue(null); // widgets have to accept null values here
        }
        this.name = createTextBox(boatToEdit.getName());
        name.ensureDebugId("NameTextBox");
        this.displayColorTextBox = createTextBox(boatToEdit.getColor() == null ? "" : boatToEdit.getColor().getAsHtml()); 
        this.sailId = createTextBox(boatToEdit.getSailId());
        sailId.ensureDebugId("SailIdTextBox");
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return name;
    }

    /**
     * Encodes an invalid color; can be used 
     * @author Axel Uhl (D043530)
     *
     */
    private class InvalidColor implements Color {
        private static final long serialVersionUID = 4012986110898149543L;
        private final Exception exception;
        
        protected InvalidColor(Exception exception) {
            this.exception = exception;
        }

        @Override
        public com.sap.sse.common.Util.Triple<Integer, Integer, Integer> getAsRGB() {
            return null;
        }

        @Override
        public com.sap.sse.common.Util.Triple<Float, Float, Float> getAsHSV() {
            return null;
        }

        @Override
        public String getAsHtml() {
            return stringMessages.invalidColor(exception.getMessage());
        }

        @Override
        public Color invert() {
            return null;
        }
        
    }

    @Override
    protected BoatDTO getResult() {
        Color color;
        if (displayColorTextBox.getText() == null || displayColorTextBox.getText().isEmpty()) {
            color = null;
        } else {
            try {
                color = new RGBColor(displayColorTextBox.getText());
            } catch (IllegalArgumentException iae) {
                color = new InvalidColor(iae);
            }
        }
        BoatClassDTO boatClass = new BoatClassDTO(boatClassName.getValue(), Distance.NULL, Distance.NULL);
        BoatDTO boat = new BoatDTO(boatToEdit.getIdAsString(), name.getText(), boatClass, sailId.getText(), color);
        return boat;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, name);
        result.setWidget(1, 0, new Label(stringMessages.sailNumber()));
        result.setWidget(1, 1, sailId);
        result.setWidget(2, 0, new Label(stringMessages.color()));
        result.setWidget(2, 1, displayColorTextBox);
        result.setWidget(3, 0, new Label(stringMessages.boatClass()));
        result.setWidget(3, 1, boatClassName);
        return result;
    }

}
