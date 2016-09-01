package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
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
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param competitorToEdit
     *            The 'competitorToEdit' parameter contains the competitor which should be changed or initialized.
     * @param boatClass
     *            The boat class is the default shown boat class for new competitors. Set <code>null</code> if your competitor is
     *            already initialized or you don�t want a default boat class.
     */
    public BoatEditDialog(final StringMessages stringMessages, BoatDTO competitorToEdit,
            DialogCallback<BoatDTO> callback, String boatClass) {
        super(stringMessages.editCompetitor(), null, stringMessages.ok(), stringMessages
                .cancel(), new Validator<BoatDTO>() {
                    @Override
                    public String getErrorMessage(BoatDTO valueToValidate) {
                        String result = null;
                        if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (valueToValidate.getSailId() == null || valueToValidate.getSailId().isEmpty()) {
                            result = stringMessages.pleaseEnterASailNumber();
                        } else if (valueToValidate.getColor() != null) {
                            Color displayColor = valueToValidate.getColor();
                            if (displayColor instanceof InvalidColor) {
                                result = displayColor.getAsHtml();
                            }
                        } else if (valueToValidate.getBoatClass().getName() == null || valueToValidate.getBoatClass().getName().isEmpty()) {
                            result = stringMessages.pleaseEnterABoatClass();
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("BoatEditDialog");
        this.stringMessages = stringMessages;
        this.boatToEdit = competitorToEdit;
                
        this.boatClassName = createSuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassName.ensureDebugId("BoatClassNameSuggestBox");
        if (competitorToEdit.getBoatClass() != null) {
            boatClassName.setValue(competitorToEdit.getBoatClass().getName());
            boatClassName.setEnabled(false);
        } else {
            boatClassName.setValue(boatClass); // widgets have to accept null values here
        }
        this.name = createTextBox(competitorToEdit.getName());
        name.ensureDebugId("NameTextBox");
        this.displayColorTextBox = createTextBox(competitorToEdit.getColor() == null ? "" : competitorToEdit.getColor().getAsHtml()); 
        this.sailId = createTextBox(competitorToEdit.getSailId());
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
        BoatClassDTO boatClass = new BoatClassDTO(boatClassName.getValue(), 0);
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
