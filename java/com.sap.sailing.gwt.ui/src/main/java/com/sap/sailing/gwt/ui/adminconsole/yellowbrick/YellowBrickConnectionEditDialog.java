package com.sap.sailing.gwt.ui.adminconsole.yellowbrick;

import com.sap.sailing.gwt.ui.shared.YellowBrickConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

/**
 * Edits a {@link YellowBrickConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link YellowBrickConnectionTableWrapper}
 */
public class YellowBrickConnectionEditDialog extends YellowBrickConnectionDialog {
    /**
     * The class creates the UI-dialog to edit the selected {@link YellowBrickConfigurationWithSecurityDTO}.
     * 
     * @param dtoToEdit
     *            The 'dtoToEdit' parameter contains the {@link YellowBrickConfigurationWithSecurityDTO} which should be edited.
     */
    public YellowBrickConnectionEditDialog(final YellowBrickConfigurationWithSecurityDTO dtoToEdit,
            final DialogCallback<YellowBrickConfigurationWithSecurityDTO> callback, final UserService userService,
            final ErrorReporter errorReporter) {
        super(callback, userService, errorReporter);
        setData(dtoToEdit);
        raceURLTextBox.setReadOnly(true);
    }

    private void setData(final YellowBrickConfigurationWithSecurityDTO dtotoEdit) {
        name = dtotoEdit.getName();
        creatorName = dtotoEdit.getCreatorName();
        raceURLTextBox.setText(dtotoEdit.getRaceUrl());
        usernameTextBox.setText(dtotoEdit.getUsername());
        passwordTextBox.setText(dtotoEdit.getPassword());
        super.getOkButton().setEnabled(!raceURLTextBox.getText().isEmpty());
    }
}
