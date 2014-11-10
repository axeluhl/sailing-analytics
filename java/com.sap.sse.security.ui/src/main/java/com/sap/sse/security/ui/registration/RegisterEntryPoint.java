package com.sap.sse.security.ui.registration;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.shared.UserDTO;

public class RegisterEntryPoint extends AbstractSecurityEntryPoint {
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        final NewAccountValidator validator = new NewAccountValidator(getStringMessages());
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);
        FlowPanel fp = new FlowPanel();
        final Label errorLabel = new Label();
        fp.add(errorLabel);
        Label nameLabel = new Label(getStringMessages().username());
        fp.add(nameLabel);
        final TextBox nameText = new TextBox();
        fp.add(nameText);
        Label emailLabel = new Label(getStringMessages().email());
        fp.add(emailLabel);
        final TextBox emailText = new TextBox();
        fp.add(emailText);
        Label pwLabel = new Label(getStringMessages().password());
        fp.add(pwLabel);
        final PasswordTextBox pwText = new PasswordTextBox();
        fp.add(pwText);
        Label pw2Label = new Label(getStringMessages().passwordRepeat());
        fp.add(pw2Label);
        final PasswordTextBox pw2Text = new PasswordTextBox();
        fp.add(pw2Text);
        final SubmitButton submit = new SubmitButton(getStringMessages().signUp());
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
                getUserManagementService().createSimpleUser(nameText.getText(), emailText.getText(), pwText.getText(),
                        EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()),
                        new AsyncCallback<UserDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage());
                        if (caught instanceof UserManagementException) {
                            String message = ((UserManagementException) caught).getMessage();
                            if (UserManagementException.USER_ALREADY_EXISTS.equals(message)) {
                                Window.alert(getStringMessages().userAlreadyExists(nameText.getText()));
                            }
                        } else {
                            Window.alert(getStringMessages().errorCreatingUser(nameText.getText(), caught.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(UserDTO result) {
                        if (result != null) {
                            Window.alert(getStringMessages().signedUpSuccessfully(result.getName()));
                        }
                        else {
                            Window.alert(getStringMessages().unknownErrorCreatingUser(nameText.getText()));
                        }
                        
                    }
                });
            }
        });
        formPanel.add(fp);
        dockPanel.add(formPanel);
    }
}
