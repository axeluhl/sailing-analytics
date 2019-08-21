package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.Optional;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
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
    private final SuggestBox suggestMarkTemplate2Optional;
    private final SuggestBox suggestPassingInstruction;

    private MarkTemplateSuggestOracle markTemplateOracle;

    public ControlPointEditDialog(final SailingServiceAsync sailingServiceAsync, final StringMessages stringMessages,
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

        markTemplateOracle = new MarkTemplateSuggestOracle(sailingServiceAsync, stringMessages);
        suggestMarkTemplate1 = new SuggestBox(markTemplateOracle);
        suggestMarkTemplate2Optional = new SuggestBox(markTemplateOracle);

        suggestPassingInstruction = new SuggestBox(new PassingInstructionSuggestOracle());

        suggestPassingInstruction.getValueBox().addChangeHandler(e -> validateAndUpdate());
        suggestPassingInstruction.getValueBox().addKeyUpHandler(e -> validateAndUpdate());
        suggestPassingInstruction.getValueBox().addBlurHandler(e -> validateAndUpdate());
        suggestMarkTemplate2Optional.getValueBox().addChangeHandler(e -> validateAndUpdate());
        suggestMarkTemplate2Optional.getValueBox().addKeyUpHandler(e -> validateAndUpdate());
        suggestMarkTemplate2Optional.getValueBox().addBlurHandler(e -> validateAndUpdate());
        suggestMarkTemplate1.getValueBox().addChangeHandler(e -> validateAndUpdate());
        suggestMarkTemplate1.getValueBox().addKeyUpHandler(e -> validateAndUpdate());
        suggestMarkTemplate1.getValueBox().addBlurHandler(e -> validateAndUpdate());
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return suggestMarkTemplate1.getValueBox();
    }

    @Override
    protected ControlPointEditDTO getResult() {
        PassingInstruction passingInstruction = null;
        try {
            passingInstruction = PassingInstruction.valueOfIgnoringCase(suggestPassingInstruction.getValue());
        } catch (IllegalArgumentException e) {
            // use null -> fail during validation
        }

        final MarkTemplateDTO markTemplate1 = markTemplateOracle.fromString(suggestMarkTemplate1.getValue());
        final MarkTemplateDTO markTemplate2 = markTemplateOracle.fromString(suggestMarkTemplate2Optional.getValue());
        return new ControlPointEditDTO(markTemplate1, markTemplate2, passingInstruction);
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(3, 2);
        result.setWidget(0, 0, new Label(stringMessages.markTemplate()));
        result.setWidget(0, 1, suggestMarkTemplate1);
        result.setWidget(1, 0, new Label(stringMessages.markTemplate2Optional()));
        result.setWidget(1, 1, suggestMarkTemplate2Optional);
        result.setWidget(2, 0, new Label(stringMessages.passingInstructions()));
        result.setWidget(2, 1, suggestPassingInstruction);
        return result;
    }

    public static class ControlPointEditDTO {
        private MarkTemplateDTO markTemplate1;
        private Optional<MarkTemplateDTO> markTemplate2;
        private PassingInstruction passingInstruction;

        public ControlPointEditDTO() {

        }

        public ControlPointEditDTO(MarkTemplateDTO markTemplate1, MarkTemplateDTO markTemplate2,
                PassingInstruction passingInstruction) {
            super();
            this.markTemplate1 = markTemplate1;
            this.markTemplate2 = Optional.ofNullable(markTemplate2);
            this.passingInstruction = passingInstruction;
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
    }

}
