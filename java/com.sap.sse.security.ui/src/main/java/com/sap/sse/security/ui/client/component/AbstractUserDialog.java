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
import com.sap.sse.security.ui.client.component.AbstractUserDialog.UserData;
import com.sap.sse.security.ui.client.component.UserManagementPanel.UserCreationEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

/**
 * Produces username, e-mail and password as the dialog's result.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AbstractUserDialog extends DataEntryDialog<UserData> {
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final TextBox emailBox;
    private final PasswordTextBox pwBox;
    private final PasswordTextBox pwRepeat;
    
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

    public AbstractUserDialog(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService,
            final Iterable<UserCreationEventHandler> handlers) {
        super(stringMessages.createUser(), stringMessages.createUser(), stringMessages.ok(), stringMessages.cancel(),
                new DataEntryDialog.Validator<UserData>() {
                    private final NewAccountValidator validator = new NewAccountValidator(stringMessages);
                    @Override
                    public String getErrorMessage(UserData valueToValidate) {
                        return validator.validate(valueToValidate.getUsername(), valueToValidate.getPassword(), valueToValidate.getPasswordRepeat());
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
        nameBox = createTextBox("", 30);
        emailBox = createTextBox("", 30);
        pwBox = createPasswordTextBox("", 30);
        pwRepeat = createPasswordTextBox("", 30);
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
        result.setWidget(0, 1, nameBox);
        result.setWidget(1, 0, new Label(stringMessages.email()));
        result.setWidget(1, 1, emailBox);
        result.setWidget(2, 0, new Label(stringMessages.password()));
        result.setWidget(2,  1, pwBox);
        result.setWidget(3, 0, new Label(stringMessages.passwordRepeat()));
        result.setWidget(3, 1, pwRepeat);
        return result;
    }

    @Override
    protected UserData getResult() {
        return new UserData(nameBox.getText(), emailBox.getText(), pwBox.getText(), pwRepeat.getText());
    }

}
