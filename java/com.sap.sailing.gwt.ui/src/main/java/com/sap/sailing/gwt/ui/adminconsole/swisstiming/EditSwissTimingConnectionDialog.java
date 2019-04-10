package com.sap.sailing.gwt.ui.adminconsole.swisstiming;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Edits a {@link SwissTimingConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link SwissTimingConnectionTableWrapper}
 */
public class EditSwissTimingConnectionDialog extends DataEntryDialog<SwissTimingConfigurationWithSecurityDTO> {
    private static final StringMessages stringMessages = StringMessages.INSTANCE;
    private Grid grid;

    private TextBox manage2SailEventIdTextBox;
    private TextBox manage2SailEventUrlJsonTextBox;
    private TextBox hostnameTextBox;
    private TextBox portTextBox;
    private TextBox updateUrlTextBox;
    private TextBox updateUsernameTextBox;
    private PasswordTextBox updatePasswordTextBox;
    private final SwissTimingConfigurationWithSecurityDTO dtoToEdit;

    /**
     * The class creates the UI-dialog to type in the Data about a the selected swiss timing account.
     * 
     * @param userToEdit
     *            The 'userToEdit' parameter contains the user which should be changed or initialized.
     */
    public EditSwissTimingConnectionDialog(final SwissTimingConfigurationWithSecurityDTO dtotoEdit,
            final DialogCallback<SwissTimingConfigurationWithSecurityDTO> callback, final UserService userService,
            final ErrorReporter errorReporter) {
        // TODO: i18n
        super("Edit Swiss Timing Connection", null, stringMessages.ok(), stringMessages.cancel(),
                /* validator */ null, /* animationEnabled */true, callback);
        this.dtoToEdit = dtotoEdit;
        this.ensureDebugId("EditTracTracConnectionDialog");
        createUi();
        setData(dtotoEdit);
    }

    private void setData(final SwissTimingConfigurationWithSecurityDTO dtotoEdit) {
        // TODO
        manage2SailEventIdTextBox.setText("?");
        manage2SailEventUrlJsonTextBox.setText(dtotoEdit.getJsonURL());
        hostnameTextBox.setText(dtotoEdit.getHostname());
        portTextBox.setText("" + dtotoEdit.getPort());
        updateUrlTextBox.setText(dtotoEdit.getUpdateURL());
        updateUsernameTextBox.setText(dtotoEdit.getUpdateUsername());
        updatePasswordTextBox.setText(dtotoEdit.getUpdatePassword());
        super.getOkButton().setEnabled(!manage2SailEventUrlJsonTextBox.getText().isEmpty());
    }

    private void createUi() {

        grid = new Grid(8, 2);
        grid.setWidget(0, 0, new Label(stringMessages.details() + ":"));

        // Manage2SailEventId
        final Label manage2SailEventIdLabel = new Label(stringMessages.manage2SailEventIdBox() + ":");
        manage2SailEventIdLabel.setTitle(stringMessages.leaveEmptyForDefault());

        manage2SailEventIdTextBox = new TextBox();
        manage2SailEventIdTextBox.ensureDebugId("Manage2SailEventIdTextBox");
        manage2SailEventIdTextBox.setVisibleLength(40);
        manage2SailEventIdTextBox.setTitle(stringMessages.manage2SailEventIdBox());

        grid.setWidget(1, 0, manage2SailEventIdLabel);
        grid.setWidget(1, 1, manage2SailEventIdTextBox);

        // Manage2SailEventUrl
        final Label manage2SailEventUrlJsonLabel = new Label(stringMessages.liveUri() + ":");
        manage2SailEventUrlJsonLabel.setTitle(stringMessages.leaveEmptyForDefault());

        manage2SailEventUrlJsonTextBox = new TextBox();
        manage2SailEventUrlJsonTextBox.ensureDebugId("Manage2SailEventUrlJsonTextBox");
        manage2SailEventUrlJsonTextBox.setVisibleLength(40);
        manage2SailEventUrlJsonTextBox.setTitle(stringMessages.manage2SailEventIdBox());

        grid.setWidget(2, 0, manage2SailEventUrlJsonLabel);
        grid.setWidget(2, 1, manage2SailEventUrlJsonTextBox);

        // Hostname
        final Label hostnameLabel = new Label(stringMessages.hostname() + ":");
        manage2SailEventUrlJsonLabel.setTitle(stringMessages.leaveEmptyForDefault());

        hostnameTextBox = new TextBox();
        hostnameTextBox.ensureDebugId("HostnameTextBox");
        hostnameTextBox.setVisibleLength(40);
        hostnameTextBox.setTitle(stringMessages.hostname());

        grid.setWidget(3, 0, hostnameLabel);
        grid.setWidget(3, 1, hostnameTextBox);

        // Port
        final Label portLabel = new Label(stringMessages.manage2SailPort() + ":");
        portLabel.setTitle(stringMessages.leaveEmptyForDefault());

        portTextBox = new TextBox();
        portTextBox.ensureDebugId("PortTextBox");
        portTextBox.setVisibleLength(40);
        portTextBox.setTitle(stringMessages.manage2SailPort());

        grid.setWidget(4, 0, portLabel);
        grid.setWidget(4, 1, portTextBox);

        // Update URL
        final Label updateUrlLabel = new Label(stringMessages.swissTimingUpdateURL() + ":");
        updateUrlLabel.setTitle(stringMessages.leaveEmptyForDefault());

        updateUrlTextBox = new TextBox();
        updateUrlTextBox.ensureDebugId("UpdateUrlTextBox");
        updateUrlTextBox.setVisibleLength(40);
        updateUrlTextBox.setTitle(stringMessages.swissTimingUpdateURL());

        grid.setWidget(5, 0, updateUrlLabel);
        grid.setWidget(5, 1, updateUrlTextBox);

        // Update Username
        final Label updateUsernameLabel = new Label(stringMessages.swissTimingUpdateUsername() + ":");
        updateUsernameLabel.setTitle(stringMessages.leaveEmptyForDefault());

        updateUsernameTextBox = new TextBox();
        updateUsernameTextBox.ensureDebugId("UpdateUsernameTextBox");
        updateUsernameTextBox.setVisibleLength(40);
        updateUsernameTextBox.setTitle(stringMessages.swissTimingUpdateUsername());

        grid.setWidget(6, 0, updateUsernameLabel);
        grid.setWidget(6, 1, updateUsernameTextBox);

        // Update Password
        final Label updatePasswordLabel = new Label(stringMessages.swissTimingUpdatePassword() + ":");
        updatePasswordLabel.setTitle(stringMessages.leaveEmptyForDefault());

        updatePasswordTextBox = new PasswordTextBox();
        updatePasswordTextBox.ensureDebugId("UpdatePasswordTextBox");
        updatePasswordTextBox.setVisibleLength(40);
        updatePasswordTextBox.setTitle(stringMessages.swissTimingUpdatePassword());

        grid.setWidget(7, 0, updatePasswordLabel);
        grid.setWidget(7, 1, updatePasswordTextBox);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return manage2SailEventUrlJsonTextBox;
    }

    @Override
    protected SwissTimingConfigurationWithSecurityDTO getResult() {
        Integer port = null;
        if (!portTextBox.getText().isEmpty()) {
            try {
                port = Integer.parseInt(portTextBox.getText());
            } catch (NumberFormatException e) {
                // port will be null.
            }
        }

        return new SwissTimingConfigurationWithSecurityDTO(dtoToEdit.getName(),
                manage2SailEventUrlJsonTextBox.getValue(), hostnameTextBox.getValue(), port,
                updateUrlTextBox.getValue(), updateUsernameTextBox.getValue(), updatePasswordTextBox.getValue(),
                dtoToEdit.getCreatorName());
    }

    @Override
    protected Widget getAdditionalWidget() {
        return grid;
    }

}
