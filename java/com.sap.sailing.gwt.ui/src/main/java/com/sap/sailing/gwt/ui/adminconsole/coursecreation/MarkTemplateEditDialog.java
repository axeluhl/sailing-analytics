package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class MarkTemplateEditDialog extends DataEntryDialog<MarkTemplateDTO> {
    private final TextBox nameTextBox;
    private final TextBox displayColorTextBox;
    private final TextBox shortNameTextBox;
    private final TextBox shapeTextBox;
    private final TextBox patternTextBox;
    private final ValueListBox<MarkType> markTypeValueListBox;
    private final StringMessages stringMessages;

    public MarkTemplateEditDialog(final StringMessages stringMessages, MarkTemplateDTO markTemplateToEdit,
            DialogCallback<MarkTemplateDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.markTemplates(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<MarkTemplateDTO>() {
                    @Override
                    public String getErrorMessage(MarkTemplateDTO valueToValidate) {
                        String result = null;
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        boolean invalidMarkType = valueToValidate.getCommonMarkProperties().getType() == null;
                        if (invalidName) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (invalidMarkType) {
                            result = stringMessages.pleaseEnterAValidValueFor(stringMessages.type(), "");
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("MarkTemplateToEditEditDialog");
        this.stringMessages = stringMessages;

        this.markTypeValueListBox = new ValueListBox<>(new Renderer<MarkType>() {

            @Override
            public String render(MarkType object) {
                return object == null ? "" : object.name();
            }

            @Override
            public void render(MarkType object, Appendable appendable) throws IOException {
                appendable.append(render(object));

            }
        });

        this.nameTextBox = createTextBox(markTemplateToEdit.getName());
        this.shortNameTextBox = createTextBox(markTemplateToEdit.getCommonMarkProperties().getShortName());
        this.shapeTextBox = createTextBox(markTemplateToEdit.getCommonMarkProperties().getShape());
        this.patternTextBox = createTextBox(markTemplateToEdit.getCommonMarkProperties().getPattern());
        this.markTypeValueListBox.setValue(markTemplateToEdit.getCommonMarkProperties().getType() != null
                ? markTemplateToEdit.getCommonMarkProperties().getType()
                : MarkType.values()[0]);
        markTypeValueListBox.setAcceptableValues(Arrays.asList(MarkType.values()));

        markTypeValueListBox.addValueChangeHandler(v -> validateAndUpdate());
        if (markTemplateToEdit.getCommonMarkProperties().getColor() != null) {
            this.displayColorTextBox = createTextBox(markTemplateToEdit.getCommonMarkProperties().getColor() == null ? ""
                    : markTemplateToEdit.getCommonMarkProperties().getColor().getAsHtml());
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
    protected MarkTemplateDTO getResult() {
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
        MarkTemplateDTO markTemplate = new MarkTemplateDTO(UUID.randomUUID(), nameTextBox.getValue(),
                shortNameTextBox.getValue(), color, shapeTextBox.getText(), patternTextBox.getText(),
                markTypeValueListBox.getValue());
        return markTemplate;
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
        result.setWidget(5, 1, markTypeValueListBox);
        return result;
    }

}
