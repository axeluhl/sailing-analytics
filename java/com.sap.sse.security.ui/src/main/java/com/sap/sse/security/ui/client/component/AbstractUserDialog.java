package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.AbstractUserDialog.UserData;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Produces username, e-mail and password as the dialog's result. This class's default implementation of
 * {@link #getAdditionalWidget()} produces fields for username, e-mail, password and repeated password.
 * Subclasses can override that method to produce their own set of controls or add other widgets to the
 * dialog.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AbstractUserDialog extends DataEntryDialog<UserData> {
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final TextBox emailBox;
    private final TextBox oldPwBox;
    private final PasswordTextBox pwBox;
    private final PasswordTextBox pwRepeat;
    private final UserManagementServiceAsync userManagementService;
    
    public static class UserData {
        private final String username;
        private final String email;
        private final String oldPassword;
        private final String password;
        private final String passwordRepeat;
        protected UserData(String username, String email, String oldPassword, String password, String passwordRepeat) {
            super();
            this.username = username;
            this.email = email;
            this.oldPassword = oldPassword;
            this.password = password;
            this.passwordRepeat = passwordRepeat;
        }
        public String getUsername() {
            return username;
        }
        public String getEmail() {
            return email;
        }
        public String getOldPassword() {
            return oldPassword;
        }
        public String getPassword() {
            return password;
        }
        public String getPasswordRepeat() {
            return passwordRepeat;
        }
    }

    /**
     * Uses a default validator that validates the username and the two new passwords.
     */
    public AbstractUserDialog(final StringMessages stringMessages, final String title,
            final UserManagementServiceAsync userManagementService,
            final UserDTO user,
            final DialogCallback<UserData> callback) {
        this(stringMessages, title, title, userManagementService, user, callback);
    }
    
    /**
     * Uses a default validator that validates the username and the two new passwords and allows
     * callers to specify distinct title and message strings.
     */
    public AbstractUserDialog(final StringMessages stringMessages, final String title, final String message,
            final UserManagementServiceAsync userManagementService,
            final UserDTO user,
            final DialogCallback<UserData> callback) {
        this(stringMessages, title, message, userManagementService, user, new DataEntryDialog.Validator<UserData>() {
            private final NewAccountValidator validator = new NewAccountValidator(stringMessages);
            @Override
            public String getErrorMessage(UserData valueToValidate) {
                return validator.validateUsernameAndPassword(valueToValidate.getUsername(), valueToValidate.getPassword(),
                        valueToValidate.getPasswordRepeat());
            }
        }, callback);
    }
    
    /**
     * Allows the caller to provide their own validator, e.g., in order to skip password or username validation or to
     * add validation for the current password
     */
    public AbstractUserDialog(final StringMessages stringMessages, final String title, final String message,
                final UserManagementServiceAsync userManagementService, final UserDTO user,
                DataEntryDialog.Validator<UserData> validator, final DialogCallback<UserData> callback) {
        super(title, message, stringMessages.ok(), stringMessages.cancel(),
                validator, callback);
        nameBox = createTextBox("", 30);
        nameBox.setName("username");
        emailBox = createTextBox("", 30);
        oldPwBox = createPasswordTextBox("", 30);
        pwBox = createPasswordTextBox("", 30);
        nameBox.setName("password");
        pwRepeat = createPasswordTextBox("", 30);
        if (user != null) {
            nameBox.setText(user.getName());
            emailBox.setText(user.getEmail());
        }
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
    }
    
    protected UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameBox;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    protected TextBox getNameBox() {
        return nameBox;
    }

    protected TextBox getEmailBox() {
        return emailBox;
    }

    protected TextBox getOldPwBox() {
        return oldPwBox;
    }

    protected PasswordTextBox getPwBox() {
        return pwBox;
    }

    protected PasswordTextBox getPwRepeat() {
        return pwRepeat;
    }

    /**
     * This default implementation does not show the field for the current password.
     */
    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(getStringMessages().username()));
        result.setWidget(0, 1, getNameBox());
        result.setWidget(1, 0, new Label(getStringMessages().email()));
        result.setWidget(1, 1, getEmailBox());
        result.setWidget(2, 0, new Label(getStringMessages().password()));
        result.setWidget(2, 1, getPwBox());
        result.setWidget(3, 0, new Label(getStringMessages().passwordRepeat()));
        result.setWidget(3, 1, getPwRepeat());
        return result;
    }

    @Override
    protected UserData getResult() {
        return new UserData(nameBox.getText(), emailBox.getText(), oldPwBox.getText(), pwBox.getText(), pwRepeat.getText());
    }

}
