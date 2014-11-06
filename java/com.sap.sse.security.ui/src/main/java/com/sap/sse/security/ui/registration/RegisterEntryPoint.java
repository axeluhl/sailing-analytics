package com.sap.sse.security.ui.registration;

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class RegisterEntryPoint implements EntryPoint {
    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    private final StringMessages stringMessages = GWT.create(StringMessages.class);

    @Override
    public void onModuleLoad() {
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                RemoteServiceMappingConstants.WEB_CONTEXT_PATH,
                RemoteServiceMappingConstants.userManagementServiceRemotePath);
        final NewAccountValidator validator = new NewAccountValidator(stringMessages);
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);
        FlowPanel fp = new FlowPanel();
        final Label errorLabel = new Label();
        fp.add(errorLabel);
        Label nameLabel = new Label(stringMessages.username());
        fp.add(nameLabel);
        final TextBox nameText = new TextBox();
        fp.add(nameText);
        Label emailLabel = new Label(stringMessages.email());
        fp.add(emailLabel);
        final TextBox emailText = new TextBox();
        fp.add(emailText);
        Label pwLabel = new Label(stringMessages.password());
        fp.add(pwLabel);
        final PasswordTextBox pwText = new PasswordTextBox();
        fp.add(pwText);
        Label pw2Label = new Label(stringMessages.passwordRepeat());
        fp.add(pw2Label);
        final PasswordTextBox pw2Text = new PasswordTextBox();
        fp.add(pw2Text);
        final SubmitButton submit = new SubmitButton(stringMessages.signUp());
        fp.add(submit);
        KeyUpHandler keyUpHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String errorMessage = validator.validateUsernameAndPassword(nameText.getText(), pwText.getText(), pw2Text.getText());
                if (errorMessage == null) {
                    errorLabel.setText("");
                    submit.setEnabled(true);
                } else {
                    errorLabel.setText(errorMessage);
                    submit.setEnabled(false);
                }
            }
        };
        keyUpHandler.onKeyUp(null); // enable/disable submit button and fill error label
        nameText.addKeyUpHandler(keyUpHandler);
        pwText.addKeyUpHandler(keyUpHandler);
        pw2Text.addKeyUpHandler(keyUpHandler);
        FormPanel formPanel = new FormPanel();
        formPanel.addSubmitHandler(new SubmitHandler() {
            @Override
            public void onSubmit(SubmitEvent event) {
                userManagementService.createSimpleUser(nameText.getText(), emailText.getText(), pwText.getText(),
                        EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()),
                        new AsyncCallback<UserDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage());
                        if (caught instanceof UserManagementException) {
                            String message = ((UserManagementException) caught).getMessage();
                            if (UserManagementException.USER_ALREADY_EXISTS.equals(message)) {
                                Window.alert(stringMessages.userAlreadyExists(nameText.getText()));
                            }
                        } else {
                            Window.alert(stringMessages.errorCreatingUser(nameText.getText(), caught.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(UserDTO result) {
                        if (result != null) {
                            Window.alert(stringMessages.signedUpSuccessfully(result.getName()));
                        }
                        else {
                            Window.alert(stringMessages.unknownErrorCreatingUser(nameText.getText()));
                        }
                        
                    }
                });
            }
        });
        formPanel.add(fp);
        dockPanel.add(formPanel);
    }
}
