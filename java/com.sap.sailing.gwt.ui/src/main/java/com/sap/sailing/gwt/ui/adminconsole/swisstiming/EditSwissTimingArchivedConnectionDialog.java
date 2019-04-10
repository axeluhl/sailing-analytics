package com.sap.sailing.gwt.ui.adminconsole.swisstiming;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Edits a {@link SwissTimingArchiveConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link SwissTimingArchivedConnectionTableWrapper}
 */
public class EditSwissTimingArchivedConnectionDialog
        extends DataEntryDialog<SwissTimingArchiveConfigurationWithSecurityDTO> {
    private static final StringMessages stringMessages = StringMessages.INSTANCE;
    private Grid grid;

    private TextBox jsonURLTextBox;
    private final SwissTimingArchiveConfigurationWithSecurityDTO dtoToEdit;

    /**
     * The class creates the UI-dialog to type in the Data about a the selected swiss timing account.
     * 
     * @param userToEdit
     *            The 'userToEdit' parameter contains the user which should be changed or initialized.
     */
    public EditSwissTimingArchivedConnectionDialog(final SwissTimingArchiveConfigurationWithSecurityDTO dtotoEdit,
            final DialogCallback<SwissTimingArchiveConfigurationWithSecurityDTO> callback,
            final UserService userService,
            final ErrorReporter errorReporter) {
        // TODO: i18n
        super("Edit swiss timing archived connection", null, stringMessages.ok(), stringMessages.cancel(),
                /* validator */ null, /* animationEnabled */true, callback);
        this.dtoToEdit = dtotoEdit;
        this.ensureDebugId("EditSwissTimingArchivedConnectionDialog");
        createUi();
        setData(dtotoEdit);
    }

    private void setData(final SwissTimingArchiveConfigurationWithSecurityDTO dtotoEdit) {
        jsonURLTextBox.setText(dtotoEdit.getJsonUrl());
        super.getOkButton().setEnabled(!jsonURLTextBox.getText().isEmpty());
    }

    private void createUi() {

        grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(stringMessages.details() + ":"));

        // JSON URL
        Label jsonURLLabel = new Label(stringMessages.jsonUrl() + ":");

        jsonURLTextBox = new TextBox();
        jsonURLTextBox.ensureDebugId("JsonURLTextBox");
        jsonURLTextBox.setVisibleLength(100);

        // validation: User should not create empty connections
        jsonURLTextBox.addKeyUpHandler(e -> super.getOkButton().setEnabled(!jsonURLTextBox.getText().isEmpty()));

        grid.setWidget(1, 0, jsonURLLabel);
        grid.setWidget(1, 1, jsonURLTextBox);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return jsonURLTextBox;
    }

    @Override
    protected SwissTimingArchiveConfigurationWithSecurityDTO getResult() {
        final String jsonURL = jsonURLTextBox.getValue();
        return new SwissTimingArchiveConfigurationWithSecurityDTO(jsonURL, dtoToEdit.getCreatorName());
    }

    @Override
    protected Widget getAdditionalWidget() {
        return grid;
    }

}
