package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.CourseTemplateDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CourseTemplateEditDialog extends DataEntryDialog<CourseTemplateDTO> {
    private final TextBox nameTextBox;
    private final StringMessages stringMessages;

    public CourseTemplateEditDialog(final StringMessages stringMessages, CourseTemplateDTO courseTemplateToEdit,
            DialogCallback<CourseTemplateDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.courseTemplates(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<CourseTemplateDTO>() {
                    @Override
                    public String getErrorMessage(CourseTemplateDTO valueToValidate) {
                        String result = null;
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        if (invalidName) {
                            result = stringMessages.pleaseEnterAName();
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("CourseTemplateToEditEditDialog");
        this.stringMessages = stringMessages;

        this.nameTextBox = createTextBox(courseTemplateToEdit.getName());

    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameTextBox;
    }


    @Override
    protected CourseTemplateDTO getResult() {
        // TODO: implement
        return new CourseTemplateDTO();
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(6, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, nameTextBox);
        return result;
    }

}
