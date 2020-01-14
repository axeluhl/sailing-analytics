package com.sap.sailing.gwt.ui.adminconsole.tractrac;

import com.sap.sailing.gwt.ui.shared.TracTracConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

/**
 * Edits a {@link TracTracConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link TracTracConnectionTableWrapper}
 */
public class TracTracConnectionEditDialog extends TracTracConnectionDialog {
    /**
     * The class creates the UI-dialog to type in the Data about a the selected trac trac account.
     * 
     * @param userToEdit
     *            The 'userToEdit' parameter contains the user which should be changed or initialized.
     */
    public TracTracConnectionEditDialog(final TracTracConfigurationWithSecurityDTO dtoToEdit,
            final DialogCallback<TracTracConfigurationWithSecurityDTO> callback, final UserService userService,
            final ErrorReporter errorReporter) {
        super(callback, userService, errorReporter);
        setData(dtoToEdit);
        jsonURLTextBox.setReadOnly(true);
    }

    private void setData(final TracTracConfigurationWithSecurityDTO dtotoEdit) {
        name = dtotoEdit.getName();
        creatorName = dtotoEdit.getCreatorName();
        storedURITextBox.setText(dtotoEdit.getStoredDataURI());
        liveURITextBox.setText(dtotoEdit.getLiveDataURI());
        jsonURLTextBox.setText(dtotoEdit.getJsonUrl());
        tracTracUpdateURITextBox.setText(dtotoEdit.getCourseDesignUpdateURI());
        tractracUsernameTextBox.setText(dtotoEdit.getTracTracUsername());
        tractracPasswordTextBox.setText(dtotoEdit.getTracTracPassword());
        super.getOkButton().setEnabled(!jsonURLTextBox.getText().isEmpty());
    }
}
