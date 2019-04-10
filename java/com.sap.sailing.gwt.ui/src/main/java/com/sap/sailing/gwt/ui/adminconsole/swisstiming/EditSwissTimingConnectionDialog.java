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
    private final String eventIdPattern = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}";
    private final String manage2sailBaseAPIUrl = "http://manage2sail.com/api/public/links/event/";
    private final String manage2sailAPIaccessToken = "?accesstoken=bDAv8CwsTM94ujZ";
    private final String manage2sailUrlAppendix = "&mediaType=json&includeRaces=true";

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
        super("Edit Swiss Timing Connection", null, stringMessages.ok(), stringMessages.cancel(), /* validator */ null,
                /* animationEnabled */true, callback);
        this.dtoToEdit = dtotoEdit;
        this.ensureDebugId("EditTracTracConnectionDialog");
        createUi();
        setData(dtotoEdit);
    }

    private void setData(final SwissTimingConfigurationWithSecurityDTO dtoToEdit) {
        manage2SailEventUrlJsonTextBox.setText(dtoToEdit.getJsonURL());
        hostnameTextBox.setText(dtoToEdit.getHostname());
        portTextBox.setText(dtoToEdit.getPort() == null ? "" : ("" + dtoToEdit.getPort()));
        updateUrlTextBox.setText(dtoToEdit.getUpdateURL());
        updateUsernameTextBox.setText(dtoToEdit.getUpdateUsername());
        updatePasswordTextBox.setText(dtoToEdit.getUpdatePassword());
        super.getOkButton().setEnabled(!manage2SailEventUrlJsonTextBox.getText().isEmpty());

        updateEventIdFromUrl(dtoToEdit.getJsonURL());
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

        // update url with changed event id
        manage2SailEventIdTextBox.addKeyUpHandler(e -> updateUrlFromEventId(manage2SailEventIdTextBox.getText()));

        grid.setWidget(1, 0, manage2SailEventIdLabel);
        grid.setWidget(1, 1, manage2SailEventIdTextBox);

        // Manage2SailEventUrl
        final Label manage2SailEventUrlJsonLabel = new Label(stringMessages.swissTimingUpdateURL() + ":");
        manage2SailEventUrlJsonLabel.setTitle(stringMessages.leaveEmptyForDefault());

        manage2SailEventUrlJsonTextBox = new TextBox();
        manage2SailEventUrlJsonTextBox.ensureDebugId("Manage2SailEventUrlJsonTextBox");
        manage2SailEventUrlJsonTextBox.setVisibleLength(40);
        manage2SailEventUrlJsonTextBox.setTitle(stringMessages.swissTimingUpdateURL());

        grid.setWidget(2, 0, manage2SailEventUrlJsonLabel);
        grid.setWidget(2, 1, manage2SailEventUrlJsonTextBox);

        // validation: User should not create empty connections + update event id from changed url
        manage2SailEventUrlJsonTextBox.addKeyUpHandler(
                e -> {
                    super.getOkButton().setEnabled(!manage2SailEventUrlJsonTextBox.getText().isEmpty());
                    updateEventIdFromUrl(manage2SailEventUrlJsonTextBox.getText());
                });
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

    /**
     * Similar to {@link #updateUrlFromEventId} this function tries to extract a M2S event Id by looking at the given
     * url in the Json Url Textbox. The value of {@link eventIdBox} is then set to the event ID inferred from the Json
     * Url.
     */
    private void updateEventIdFromUrl(String eventUrl) {
        if (eventUrl.matches("http://manage2sail.com/.*" + eventIdPattern + ".*")) {
            final String inferredEventId = eventUrl.replaceFirst(".*(" + eventIdPattern + ").*", "$1");
            manage2SailEventIdTextBox.setValue(inferredEventId);
        }
    }

    /**
     * This function tries to infer a valid JsonUrl for any input given that matches the pattern of an event Id from
     * M2S. If there is an event id detected the Json Url gets updated and the event Id textbox is filled with the
     * detected event Id. The ID pattern is defined in {@link eventIdPattern}.
     */
    private void updateUrlFromEventId(String eventIdText) {
        if (eventIdText.matches(".*" + eventIdPattern + ".*")) {
            final String inferredEventId = eventIdText.replaceFirst(".*(" + eventIdPattern + ").*", "$1");
            manage2SailEventUrlJsonTextBox.setValue(
                    manage2sailBaseAPIUrl + inferredEventId + manage2sailAPIaccessToken + manage2sailUrlAppendix);
            manage2SailEventIdTextBox.setValue(inferredEventId);
        }
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
