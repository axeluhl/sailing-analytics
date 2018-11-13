package com.sap.sse.security.ui.registration;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.controls.PasswordTextBoxWithWatermark;
import com.sap.sse.gwt.client.controls.TextBoxWithWatermark;
import com.sap.sse.gwt.client.dialog.DialogUtils;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class RegisterView extends Composite {
    private static RegisterViewUiBinder uiBinder = GWT.create(RegisterViewUiBinder.class);

    interface RegisterViewUiBinder extends UiBinder<Widget, RegisterView> {
    }

    private final UserManagementServiceAsync userManagementService;
    private final StringMessages stringMessages;
    
    @UiField Label appNameLabel;
    @UiField Label errorMessageLabel;
    @UiField Button signUpButton;
    @UiField TextBoxWithWatermark usernameTextBox;
    @UiField TextBoxWithWatermark emailTextBox;
    @UiField PasswordTextBoxWithWatermark passwordTextBox;
    @UiField PasswordTextBoxWithWatermark password2TextBox;

    public RegisterView(UserManagementServiceAsync userManagementService, StringMessages stringMessages, String appName) {
        this.userManagementService = userManagementService;
        this.stringMessages = stringMessages;

        RegisterViewResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        appNameLabel.setText(appName);
        final NewAccountValidator validator = new NewAccountValidator(stringMessages);
        KeyUpHandler keyUpHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String errorMessage = validator.validateUsernameAndPassword(usernameTextBox.getText(), passwordTextBox.getText(), password2TextBox.getText());
                if (errorMessage == null) {
                    errorMessageLabel.setText("");
                    signUpButton.setEnabled(true);
                    signUpButton.removeStyleName(RegisterViewResources.INSTANCE.css().buttoninactive());
                } else {
                    errorMessageLabel.setText(errorMessage);
                    signUpButton.setEnabled(false);
                    signUpButton.addStyleName(RegisterViewResources.INSTANCE.css().buttoninactive());
                }
            }
        };
        keyUpHandler.onKeyUp(null); // enable/disable submit button and fill error label
        usernameTextBox.addKeyUpHandler(keyUpHandler);
        passwordTextBox.addKeyUpHandler(keyUpHandler);
        password2TextBox.addKeyUpHandler(keyUpHandler);
        
        DialogUtils.linkEnterToButton(signUpButton, usernameTextBox, emailTextBox, passwordTextBox, password2TextBox);
    }

    @UiHandler("signUpButton")
    void signUpButtonClicked(ClickEvent e) {
        userManagementService.createSimpleUser(usernameTextBox.getText(), emailTextBox.getText(), passwordTextBox.getText(),
                /* fullName */ null, /* company */ null, LocaleInfo.getCurrentLocale().getLocaleName(),
                EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()),
                new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof UserManagementException) {
                    String message = ((UserManagementException) caught).getMessage();
                    if (UserManagementException.USER_ALREADY_EXISTS.equals(message)) {
                        Notification.notify(stringMessages.userAlreadyExists(usernameTextBox.getText()), NotificationType.ERROR);
                    }
                } else {
                    Notification.notify(stringMessages.errorCreatingUser(usernameTextBox.getText(), caught.getMessage()), NotificationType.ERROR);
                }
            }

            @Override
            public void onSuccess(UserDTO result) {
                if (result != null) {
                    Notification.notify(stringMessages.signedUpSuccessfully(result.getName()), NotificationType.SUCCESS);
                    closeWindow();
                } else {
                    Notification.notify(stringMessages.unknownErrorCreatingUser(usernameTextBox.getText()), NotificationType.ERROR);
                }
            }
        });
    }
    
    public native void closeWindow()
    /*-{
        $wnd.close();
    }-*/;
}
