package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.impl.RadianPosition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkPropertiesDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class MarkPropertiesEditDialog extends DataEntryDialog<MarkPropertiesDTO> {
    private final TextBox nameTextBox;
    private final TextBox displayColorTextBox;
    private final TextBox shortNameTextBox;
    private final TextBox shapeTextBox;
    private final TextBox patternTextBox;
    // TODO: use dropdown ui element
    private final SuggestBox markTypeSuggestBox;
    private final StringMessages stringMessages;

    public MarkPropertiesEditDialog(final StringMessages stringMessages, MarkPropertiesDTO markPropertiesToEdit,
            DialogCallback<MarkPropertiesDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.markProperties(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<MarkPropertiesDTO>() {
                    @Override
                    public String getErrorMessage(MarkPropertiesDTO valueToValidate) {
                        String result = null;
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        boolean invalidColor = valueToValidate.getMarkProperties().getColor() == null;
                        boolean invalidMarkType = valueToValidate.getMarkProperties().getType() == null;
                        if (invalidName) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (invalidColor) {
                            result = stringMessages.pleaseEnterAValidValueFor(stringMessages.color(), "");
                        } else if (invalidMarkType) {
                            result = stringMessages.pleaseEnterAValidValueFor(stringMessages.type(), "");
                        } else if (valueToValidate.getMarkProperties().getColor() != null
                                && valueToValidate.getMarkProperties().getColor() instanceof InvalidColor) {
                            result = valueToValidate.getMarkProperties().getColor().getAsHtml();
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("MarkPropertiesToEditEditDialog");
        this.stringMessages = stringMessages;

        final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        final Collection<String> markTypeStrings = Stream.of(MarkType.values()).map(MarkType::name)
                .collect(Collectors.toList());
        oracle.addAll(markTypeStrings);
        oracle.setDefaultSuggestionsFromText(markTypeStrings);
        this.markTypeSuggestBox = createSuggestBox(oracle);

        this.nameTextBox = createTextBox(markPropertiesToEdit.getName());
        this.shortNameTextBox = createTextBox(markPropertiesToEdit.getMarkProperties().getShortName());
        this.shapeTextBox = createTextBox(markPropertiesToEdit.getMarkProperties().getShape());
        this.patternTextBox = createTextBox(markPropertiesToEdit.getMarkProperties().getPattern());
        this.markTypeSuggestBox.setValue(markPropertiesToEdit.getMarkProperties().getType() != null
                ? markPropertiesToEdit.getMarkProperties().getType().name()
                : "");

        if (markPropertiesToEdit.getMarkProperties().getColor() != null) {
            this.displayColorTextBox = createTextBox(markPropertiesToEdit.getMarkProperties().getColor() == null ? ""
                    : markPropertiesToEdit.getMarkProperties().getColor().getAsHtml());
        } else {
            this.displayColorTextBox = createTextBox("");
        }
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameTextBox;
    }

    /**
     * Encodes an invalid color; can be used
     * 
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
    protected MarkPropertiesDTO getResult() {
        Color color;
        if (displayColorTextBox.getValue() == null || displayColorTextBox.getValue().isEmpty()) {
            color = null;
        } else {
            try {
                color = new RGBColor(displayColorTextBox.getText());
            } catch (IllegalArgumentException iae) {
                color = new InvalidColor(iae);
            }
        }
        MarkType markType = null;
        try {
            markType = markTypeSuggestBox.getValue().isEmpty() ? null : MarkType.valueOf(markTypeSuggestBox.getValue());
        } catch (IllegalArgumentException e) {
            GWT.log("Invalid mark type " + markTypeSuggestBox.getValue());
        }
        // TODO: tags, device identifier, position
        MarkPropertiesDTO markProperties = new MarkPropertiesDTO(UUID.randomUUID(), nameTextBox.getValue(),
                new ArrayList<String>(), new DeviceIdentifierDTO(null, null), new RadianPosition(0, 0),
                shortNameTextBox.getValue(), color, shapeTextBox.getText(), patternTextBox.getText(), markType);
        return markProperties;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(6, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, nameTextBox);
        result.setWidget(1, 0, new Label(stringMessages.shortName()));
        result.setWidget(1, 1, shortNameTextBox);
        result.setWidget(2, 0, new Label(stringMessages.color()));
        result.setWidget(2, 1, displayColorTextBox);
        result.setWidget(3, 0, new Label(stringMessages.shape()));
        result.setWidget(3, 1, shapeTextBox);
        result.setWidget(4, 0, new Label(stringMessages.pattern()));
        result.setWidget(4, 1, patternTextBox);
        result.setWidget(5, 0, new Label(stringMessages.type()));
        result.setWidget(5, 1, markTypeSuggestBox);
        return result;
    }

}
