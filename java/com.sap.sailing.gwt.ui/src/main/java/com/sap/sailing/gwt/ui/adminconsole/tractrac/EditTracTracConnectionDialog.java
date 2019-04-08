package com.sap.sailing.gwt.ui.adminconsole.tractrac;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Edits a {@link TracTracConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link TracTracConnectionTableWrapper}
 */
public class EditTracTracConnectionDialog extends DataEntryDialog<TracTracConfigurationWithSecurityDTO> {
    private static final StringMessages stringMessages = StringMessages.INSTANCE;
    private Grid grid;

    private TextBox storedURITextBox;
    private TextBox liveURITextBox;
    private TextBox jsonURLTextBox;
    private TextBox tracTracUpdateURITextBox;
    private TextBox tractracUsernameTextBox;
    private PasswordTextBox tractracPasswordTextBox;
    private final TracTracConfigurationWithSecurityDTO dtoToEdit;

    /**
     * The class creates the UI-dialog to type in the Data about a the selected trac trac account.
     * 
     * @param userToEdit
     *            The 'userToEdit' parameter contains the user which should be changed or initialized.
     */
    public EditTracTracConnectionDialog(final TracTracConfigurationWithSecurityDTO dtotoEdit,
            final DialogCallback<TracTracConfigurationWithSecurityDTO> callback, final UserService userService,
            final ErrorReporter errorReporter) {
        // TODO: i18n
        super(stringMessages.editTracTracConnection(), null, stringMessages.ok(), stringMessages.cancel(),
                /* validator */ null, /* animationEnabled */true, callback);
        this.dtoToEdit = dtotoEdit;
        this.ensureDebugId("EditTracTracConnectionDialog");
        createUi();
        setData(dtotoEdit);
    }

    private void setData(final TracTracConfigurationWithSecurityDTO dtotoEdit) {
        storedURITextBox.setText(dtotoEdit.getStoredDataURI());
        liveURITextBox.setText(dtotoEdit.getLiveDataURI());
        jsonURLTextBox.setText(dtotoEdit.getJSONURL());
        tracTracUpdateURITextBox.setText(dtotoEdit.getCourseDesignUpdateURI());
        tractracUsernameTextBox.setText(dtotoEdit.getTracTracUsername());
        tractracPasswordTextBox.setText(dtotoEdit.getTracTracPassword());
        super.getOkButton().setEnabled(!jsonURLTextBox.getText().isEmpty());
    }

    private void createUi() {

        grid = new Grid(7, 2);
        grid.setWidget(0, 0, new Label(stringMessages.details() + ":"));

        Label liveURILabel = new Label(stringMessages.liveUri() + ":");
        liveURILabel.setTitle(stringMessages.leaveEmptyForDefault());

        liveURITextBox = new TextBox();
        liveURITextBox.ensureDebugId("LiveURITextBox");
        liveURITextBox.setVisibleLength(40);
        liveURITextBox.setTitle(stringMessages.leaveEmptyForDefault());

        grid.setWidget(1, 0, liveURILabel);
        grid.setWidget(1, 1, liveURITextBox);

        Label storedURILabel = new Label(stringMessages.storedUri() + ":");
        storedURILabel.setTitle(stringMessages.leaveEmptyForDefault());

        storedURITextBox = new TextBox();
        storedURITextBox.ensureDebugId("StoredURITextBox");
        storedURITextBox.setVisibleLength(40);
        storedURITextBox.setTitle(stringMessages.leaveEmptyForDefault());

        grid.setWidget(2, 0, storedURILabel);
        grid.setWidget(2, 1, storedURITextBox);

        // JSON URL
        Label jsonURLLabel = new Label(stringMessages.jsonUrl() + ":");

        jsonURLTextBox = new TextBox();
        jsonURLTextBox.ensureDebugId("JsonURLTextBox");
        jsonURLTextBox.setVisibleLength(100);

        // validation: User should not create empty connections
        jsonURLTextBox.addKeyUpHandler(e -> super.getOkButton().setEnabled(!jsonURLTextBox.getText().isEmpty()));

        grid.setWidget(3, 0, jsonURLLabel);
        grid.setWidget(3, 1, jsonURLTextBox);

        // Course design Update URL
        Label tracTracUpdateURLLabel = new Label(stringMessages.tracTracUpdateUrl() + ":");

        tracTracUpdateURITextBox = new TextBox();
        tracTracUpdateURITextBox.ensureDebugId("TracTracUpdateURITextBox");
        tracTracUpdateURITextBox.setVisibleLength(100);

        grid.setWidget(4, 0, tracTracUpdateURLLabel);
        grid.setWidget(4, 1, tracTracUpdateURITextBox);

        // TracTrac Username
        tractracUsernameTextBox = new TextBox();
        tractracUsernameTextBox.ensureDebugId("TracTracUsernameTextBox");
        tractracUsernameTextBox.setVisibleLength(40);

        grid.setWidget(5, 0, new Label(stringMessages.tractracUsername() + ":"));
        grid.setWidget(5, 1, tractracUsernameTextBox);

        // TracTrac Password
        tractracPasswordTextBox = new PasswordTextBox();
        tractracPasswordTextBox.ensureDebugId("TracTracPasswordTextBox");
        tractracPasswordTextBox.setVisibleLength(40);

        grid.setWidget(6, 0, new Label(stringMessages.tractracPassword() + ":"));
        grid.setWidget(6, 1, tractracPasswordTextBox);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return jsonURLTextBox;
    }

    @Override
    protected TracTracConfigurationWithSecurityDTO getResult() {

        final String jsonURL = jsonURLTextBox.getValue();
        final String liveDataURI = liveURITextBox.getValue();
        final String storedDataURI = storedURITextBox.getValue();
        final String courseDesignUpdateURI = tracTracUpdateURITextBox.getValue();
        final String tractracUsername = tractracUsernameTextBox.getValue();
        final String tractracPassword = tractracPasswordTextBox.getValue();

        return new TracTracConfigurationWithSecurityDTO(dtoToEdit.getName(), jsonURL, liveDataURI, storedDataURI,
                courseDesignUpdateURI, tractracUsername, tractracPassword, dtoToEdit.getCreatorName());
    }

    @Override
    protected Widget getAdditionalWidget() {
        return grid;
    }

}
