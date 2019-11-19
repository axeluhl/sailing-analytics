package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.UUID;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkRoleDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class MarkRoleEditDialog extends DataEntryDialog<MarkRoleDTO> {

    private final StringMessages stringMessages;
    private final TextBox nameTextBox;
    private final UUID id;

    public MarkRoleEditDialog(final StringMessages stringMessages, MarkRoleDTO markRoleToEdit,
            DialogCallback<MarkRoleDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.markProperties(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<MarkRoleDTO>() {
                    @Override
                    public String getErrorMessage(MarkRoleDTO valueToValidate) {
                        String result = null;
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        if (invalidName) {
                            result = stringMessages.pleaseEnterAName();
                        }
                        return result;
                    }
                }, /* animationEnabled */ true, callback);
        this.ensureDebugId("MarkPropertiesToEditEditDialog");
        id = markRoleToEdit.getUuid();
        this.stringMessages = stringMessages;
        this.nameTextBox = createTextBox(markRoleToEdit.getName());
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameTextBox;
    }

    @Override
    protected MarkRoleDTO getResult() {
        return new MarkRoleDTO(id, nameTextBox.getValue());
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(7, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, nameTextBox);
        return result;
    }

}
