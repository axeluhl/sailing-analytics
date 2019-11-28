package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.ControlPointEditDialog.ControlPointEditDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class ControlPointEditDialog extends DataEntryDialog<ControlPointEditDTO> {
    private final StringMessages stringMessages;

    private final SuggestBox suggestMarkTemplate1;
    private final SuggestBox suggestMarkTemplate2;

    private final ValueListBox<PassingInstruction> markPassingInstructionChooser;
    private final TextBox textControlPointName;
    private final TextBox textControlPointShortName;

    private MarkTemplateSuggestOracle markTemplateOracle;

    private Label labelMarkTemplate2;
    private Label labelControlPointName;
    private Label labelControlPointShortName;

    public ControlPointEditDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            ControlPointEditDTO courseTemplateToEdit, DialogCallback<ControlPointEditDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.waypoints(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<ControlPointEditDTO>() {
                    @Override
                    public String getErrorMessage(ControlPointEditDTO valueToValidate) {
                        String result = null;

                        if (valueToValidate.getPassingInstruction() == null) {
                            result = stringMessages.pleaseSelectAValidPassingInstruction();
                        } else {
                            if (valueToValidate.getMarkTemplate1() == null) {
                                result = stringMessages.pleaseSelectAValidMarkTemplate();
                            }
                            if (valueToValidate.getPassingInstruction().applicability[0] == 2
                                    && !valueToValidate.getMarkTemplate2().isPresent()) {
                                result = stringMessages.pleaseSelectAValidMarkTemplate() + " (2)";
                            }
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("CourseTemplateToEditEditDialog");
        this.stringMessages = stringMessages;

        labelMarkTemplate2 = new Label(stringMessages.markTemplate2());
        labelControlPointName = new Label(stringMessages.name());
        labelControlPointShortName = new Label(stringMessages.shortName());

        markTemplateOracle = new MarkTemplateSuggestOracle(sailingService, stringMessages);
        suggestMarkTemplate1 = new SuggestBox(markTemplateOracle);
        suggestMarkTemplate2 = new SuggestBox(markTemplateOracle);

        suggestMarkTemplate2.getValueBox().addChangeHandler(e -> validateAndUpdate());
        suggestMarkTemplate2.getValueBox().addKeyUpHandler(e -> validateAndUpdate());
        suggestMarkTemplate2.getValueBox().addBlurHandler(e -> validateAndUpdate());
        suggestMarkTemplate1.getValueBox().addChangeHandler(e -> validateAndUpdate());
        suggestMarkTemplate1.getValueBox().addKeyUpHandler(e -> validateAndUpdate());
        suggestMarkTemplate1.getValueBox().addBlurHandler(e -> validateAndUpdate());

        markPassingInstructionChooser = new ValueListBox<>(new Renderer<PassingInstruction>() {
            @Override
            public String render(PassingInstruction object) {
                return object == null ? "" : object.name();
            }

            @Override
            public void render(PassingInstruction object, Appendable appendable) throws IOException {
                appendable.append(render(object));
            }
        });

        textControlPointName = new TextBox();
        textControlPointShortName = new TextBox();
        markPassingInstructionChooser.setValue(PassingInstruction.relevantValues()[0]);
        markPassingInstructionChooser.setAcceptableValues(Arrays.asList(PassingInstruction.relevantValues()));
        markPassingInstructionChooser.addValueChangeHandler(v -> handlePassingInstructionChange());
        handlePassingInstructionChange();
    }

    private void handlePassingInstructionChange() {
        final boolean doubleMarkPassing = markPassingInstructionChooser.getValue().applicability[0] == 2;
        suggestMarkTemplate2.setVisible(doubleMarkPassing);
        labelMarkTemplate2.setVisible(doubleMarkPassing);
        labelControlPointName.setVisible(doubleMarkPassing);
        textControlPointName.setVisible(doubleMarkPassing);
        validateAndUpdate();
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return suggestMarkTemplate1.getValueBox();
    }

    @Override
    protected ControlPointEditDTO getResult() {
        final MarkTemplateDTO markTemplate1 = markTemplateOracle.fromString(suggestMarkTemplate1.getValue());
        final MarkTemplateDTO markTemplate2 = markTemplateOracle.fromString(suggestMarkTemplate2.getValue());
        return new ControlPointEditDTO(markTemplate1, markTemplate2, markPassingInstructionChooser.getValue(),
                textControlPointName.getText(), textControlPointShortName.getText());
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(5, 2);
        result.setWidget(0, 0, new Label(stringMessages.passingInstructions()));
        result.setWidget(0, 1, markPassingInstructionChooser);
        result.setWidget(1, 0, new Label(stringMessages.markTemplate()));
        result.setWidget(1, 1, suggestMarkTemplate1);
        result.setWidget(2, 0, labelMarkTemplate2);
        result.setWidget(2, 1, suggestMarkTemplate2);
        result.setWidget(3, 0, labelControlPointName);
        result.setWidget(3, 1, textControlPointName);
        result.setWidget(4, 0, labelControlPointShortName);
        result.setWidget(4, 1, textControlPointShortName);
        return result;
    }

    public static class ControlPointEditDTO {
        private MarkTemplateDTO markTemplate1;
        private Optional<MarkTemplateDTO> markTemplate2;
        private PassingInstruction passingInstruction;
        private Optional<String> name;
        private String shortName;

        public ControlPointEditDTO() {

        }

        public ControlPointEditDTO(MarkTemplateDTO markTemplate1, MarkTemplateDTO markTemplate2,
                PassingInstruction passingInstruction, String optionalName, String shortName) {
            super();
            this.markTemplate1 = markTemplate1;
            this.name = Optional.ofNullable(optionalName);
            this.markTemplate2 = Optional.ofNullable(markTemplate2);
            this.passingInstruction = passingInstruction;
            this.shortName = shortName;
        }

        public MarkTemplateDTO getMarkTemplate1() {
            return markTemplate1;
        }

        public Optional<MarkTemplateDTO> getMarkTemplate2() {
            return markTemplate2;
        }

        public PassingInstruction getPassingInstruction() {
            return passingInstruction;
        }

        public Optional<String> getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

    }

}
