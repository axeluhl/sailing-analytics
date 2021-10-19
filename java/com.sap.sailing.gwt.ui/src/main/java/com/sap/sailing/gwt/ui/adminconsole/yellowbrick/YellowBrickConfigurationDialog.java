package com.sap.sailing.gwt.ui.adminconsole.yellowbrick;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.TracTracEventManagementPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.YellowBrickConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Creates a {@link TracTracConfigurationWithSecurityDTO} object. Can be accessed from
 * {@link TracTracEventManagementPanel}
 */
public class YellowBrickConfigurationDialog extends DataEntryDialog<YellowBrickConfigurationWithSecurityDTO> {
    private static final StringMessages stringMessages = StringMessages.INSTANCE;
    private Grid grid;

    protected TextBox raceURLTextBox;
    protected TextBox usernameTextBox;
    protected PasswordTextBox passwordTextBox;
    protected String creatorName;
    protected String name;

    /**
     * The class creates the UI-dialog to create a {@link TracTracConfigurationWithSecurityDTO}.
     */
    public YellowBrickConfigurationDialog(
            final DialogCallback<YellowBrickConfigurationWithSecurityDTO> callback, final UserService userService,
            final ErrorReporter errorReporter) {
        super(stringMessages.yellowBrickConfiguration(), /* message */null, stringMessages.ok(), stringMessages.cancel(),
                /* validator */ null, /* animationEnabled */true, callback);
        this.ensureDebugId("TracTracConnectionDialog");
        createUi();
    }

    private void createUi() {
        grid = new Grid(7, 2);
        grid.setWidget(0, 0, new Label(stringMessages.details() + ":"));
        // Race URL
        Label raceURLLabel = new Label(stringMessages.raceUrl() + ":");
        raceURLTextBox = createTextBox("");
        raceURLTextBox.ensureDebugId("RaceURLTextBox");
        raceURLTextBox.setVisibleLength(100);
        // validation: User should not create empty connections
        raceURLTextBox.addKeyUpHandler(e -> super.getOkButton().setEnabled(!raceURLTextBox.getText().isEmpty()));
        grid.setWidget(3, 0, raceURLLabel);
        grid.setWidget(3, 1, raceURLTextBox);
        // TracTrac Username
        usernameTextBox = createTextBox("");
        usernameTextBox.ensureDebugId("YellowBrickUsernameTextBox");
        usernameTextBox.setVisibleLength(40);
        grid.setWidget(5, 0, new Label(stringMessages.username() + ":"));
        grid.setWidget(5, 1, usernameTextBox);
        // TracTrac Password
        passwordTextBox = createPasswordTextBox("");
        passwordTextBox.ensureDebugId("YellowBrickPasswordTextBox");
        passwordTextBox.setVisibleLength(40);
        grid.setWidget(6, 0, new Label(stringMessages.password() + ":"));
        grid.setWidget(6, 1, passwordTextBox);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return raceURLTextBox;
    }

    @Override
    protected YellowBrickConfigurationWithSecurityDTO getResult() {
        final String raceURL = raceURLTextBox.getValue();
        final String username = usernameTextBox.getValue();
        final String password = passwordTextBox.getValue();
        return new YellowBrickConfigurationWithSecurityDTO(/* security information */ null, name, raceURL, username, password, creatorName);
    }

    @Override
    protected Widget getAdditionalWidget() {
        return grid;
    }

}
