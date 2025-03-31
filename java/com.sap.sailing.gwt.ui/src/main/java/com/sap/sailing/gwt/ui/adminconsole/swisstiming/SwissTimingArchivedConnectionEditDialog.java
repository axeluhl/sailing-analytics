package com.sap.sailing.gwt.ui.adminconsole.swisstiming;

import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

/**
 * Edits a {@link SwissTimingArchiveConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link SwissTimingArchivedConnectionTableWrapper}
 */
public class SwissTimingArchivedConnectionEditDialog extends SwissTimingArchivedConnectionDialog {
    /**
     * The class creates the UI-dialog to the selected {@link SwissTimingArchiveConfigurationWithSecurityDTO}.
     * 
     * @param dtoToEdit
     *            The 'dtoToEdit' parameter contains the {@link SwissTimingArchiveConfigurationWithSecurityDTO} which should be changed
     */
    public SwissTimingArchivedConnectionEditDialog(final SwissTimingArchiveConfigurationWithSecurityDTO dtoToEdit,
            final DialogCallback<SwissTimingArchiveConfigurationWithSecurityDTO> callback,
            final UserService userService, final ErrorReporter errorReporter) {
        super(callback, userService, errorReporter);
        setData(dtoToEdit);
        jsonURLTextBox.setReadOnly(true);
    }

    private void setData(final SwissTimingArchiveConfigurationWithSecurityDTO dtotoEdit) {
        creatorName = dtotoEdit.getCreatorName();
        jsonURLTextBox.setText(dtotoEdit.getJsonUrl());
        validateAndUpdate();
    }
}
