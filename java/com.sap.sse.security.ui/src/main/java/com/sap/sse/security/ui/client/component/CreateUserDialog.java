package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.component.CreateUserDialog.UserData;
import com.sap.sse.security.ui.client.component.UserManagementPanel.UserCreationEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

/**
 * Produces username, e-mail and password as the dialog's result.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CreateUserDialog extends DataEntryDialog<UserData> {
    private static final int MINIMUM_USERNAME_LENGTH = 3;
    private static final int MINIMUM_PASSWORD_LENGTH = 5;
    
    private final StringMessages stringMessages;
    private TextBox nameBox;
    private TextBox emailBox;
    private PasswordTextBox pwBox;
    private PasswordTextBox pwRepeat;
    
    public static class UserData {
        private final String username;
        private final String email;
        private final String password;
        private final String passwordRepeat;
        protected UserData(String username, String email, String password, String passwordRepeat) {
            super();
            this.username = username;
            this.email = email;
            this.password = password;
            this.passwordRepeat = passwordRepeat;
        }
        public String getUsername() {
            return username;
        }
        public String getEmail() {
            return email;
        }
        public String getPassword() {
            return password;
        }
        public String getPasswordRepeat() {
            return passwordRepeat;
        }
    }

    public CreateUserDialog(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService,
            final Iterable<UserCreationEventHandler> handlers) {
        super(stringMessages.createUser(), stringMessages.createUser(), stringMessages.ok(), stringMessages.cancel(),
                new DataEntryDialog.Validator<UserData>() {
                    @Override
                    public String getErrorMessage(UserData valueToValidate) {
                        final String result;
                        if (valueToValidate.getUsername().length() < MINIMUM_USERNAME_LENGTH) {
                            result = stringMessages.usernameMustHaveAtLeastNCharacters(MINIMUM_USERNAME_LENGTH);
                        } else if (valueToValidate.getPassword().length() < MINIMUM_PASSWORD_LENGTH) {
                            result = stringMessages.passwordMustHaveAtLeastNCharacters(MINIMUM_PASSWORD_LENGTH);
                        } else if (!valueToValidate.getPassword().equals(valueToValidate.getPasswordRepeat())) {
                            result = stringMessages.passwordsDontMatch();
                        } else {
                            result = null;
                        }
                        return result;
                    }
                }, new DialogCallback<UserData>() {
                    @Override
                    public void ok(UserData usernameEmailPassword) {
                        userManagementService.createSimpleUser(usernameEmailPassword.getUsername(), usernameEmailPassword.getEmail(), usernameEmailPassword.getPassword(), new AsyncCallback<UserDTO>() {
                            @Override
                            public void onSuccess(UserDTO result) {
                                for (UserCreationEventHandler handler : handlers) {
                                    handler.onUserCreation(result);
                                }
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.couldNotCreateUser(caught.getMessage()));
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
                });
        this.stringMessages = stringMessages;
    }
    
    @Override
    public void show() {
        super.show();
        nameBox.setFocus(true);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(stringMessages.username()));
        nameBox = createTextBox("", 30);
        result.setWidget(0, 1, nameBox);
        result.setWidget(1, 0, new Label(stringMessages.email()));
        emailBox = createTextBox("", 30);
        result.setWidget(1, 1, emailBox);
        result.setWidget(2, 0, new Label(stringMessages.password()));
        pwBox = createPasswordTextBox("", 30);
        result.setWidget(2,  1, pwBox);
        result.setWidget(3, 0, new Label(stringMessages.passwordRepeat()));
        pwRepeat = createPasswordTextBox("", 30);
        result.setWidget(3, 1, pwRepeat);
        return result;
    }

    @Override
    protected UserData getResult() {
        return new UserData(nameBox.getText(), emailBox.getText(), pwBox.getText(), pwRepeat.getText());
    }

}
