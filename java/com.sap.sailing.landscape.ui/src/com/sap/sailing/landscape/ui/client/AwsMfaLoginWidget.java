package com.sap.sailing.landscape.ui.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DialogUtils;
import com.sap.sse.security.ui.client.UserService;

public class AwsMfaLoginWidget extends VerticalPanel implements AwsAccessKeyProvider {
    private final static String AWS_ACCESS_KEY_USER_PREFERENCE = "aws.access.key";

    private final TextBox awsAccessKeyTextBox;
    private final PasswordTextBox awsSecretPasswordTextBox;
    private final TextBox mfaTokenCodeTextBox;
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final ErrorReporter errorReporter;
    private final Grid awsCredentialsGrid;
    private final Set<AwsMfaLoginListener> listeners;
    private boolean hasValidSessionCredentials;
    
    @FunctionalInterface
    public static interface AwsMfaLoginListener {
        void awsSessionStatusChanged(boolean hasValidSessionCredentials);
    }
    
    public AwsMfaLoginWidget(LandscapeManagementWriteServiceAsync landscapeManagementService, ErrorReporter errorReporter,
            UserService userService, StringMessages stringMessages) {
        super();
        this.listeners = new HashSet<>();
        this.landscapeManagementService = landscapeManagementService;
        this.errorReporter = errorReporter;
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        this.add(buttonPanel);
        final Button refreshButton = new Button(stringMessages.refresh());
        buttonPanel.add(refreshButton);
        final Button logoutButton = new Button(stringMessages.logout());
        buttonPanel.add(logoutButton);
        awsCredentialsGrid = new Grid(4, 2);
        this.add(awsCredentialsGrid);
        awsCredentialsGrid.setWidget(0, 0, new Label(stringMessages.awsAccessKey()));
        awsAccessKeyTextBox = new TextBox();
        userService.getPreference(AWS_ACCESS_KEY_USER_PREFERENCE, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
    
            @Override
            public void onSuccess(String result) {
                awsAccessKeyTextBox.setValue(result);
            }
        });
        awsAccessKeyTextBox.addValueChangeHandler(e->userService.setPreference(AWS_ACCESS_KEY_USER_PREFERENCE, awsAccessKeyTextBox.getValue(),
                new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
            @Override
            public void onSuccess(Void result) {
            }
        }));
        awsCredentialsGrid.setWidget(0, 1, awsAccessKeyTextBox);
        awsCredentialsGrid.setWidget(1, 0, new Label(stringMessages.awsSecret()));
        awsSecretPasswordTextBox = new PasswordTextBox();
        awsCredentialsGrid.setWidget(1, 1, awsSecretPasswordTextBox);
        mfaTokenCodeTextBox = new TextBox();
        awsCredentialsGrid.setWidget(2, 0, new Label(stringMessages.mfaTokenCode()));
        awsCredentialsGrid.setWidget(2, 1, mfaTokenCodeTextBox);
        final Button loginButton = new Button(stringMessages.login());
        awsCredentialsGrid.setWidget(3, 0, loginButton);
        awsCredentialsGrid.setVisible(false);
        refreshButton.addClickHandler(e->checkSessionCredentials());
        loginButton.addClickHandler(e->landscapeManagementService.createMfaSessionCredentials(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(), mfaTokenCodeTextBox.getValue(),
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.invalidCredentialsNoSessionCreated(caught.getMessage()));
                        checkSessionCredentials();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(stringMessages.loggedInSuccessfully(), NotificationType.SUCCESS);
                        checkSessionCredentials();
                    }
                }));
        logoutButton.addClickHandler(e->landscapeManagementService.clearSessionCredentials(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
                checkSessionCredentials();
            }

            @Override
            public void onSuccess(Void result) {
                Notification.notify(stringMessages.loggedOutSuccessfully(), NotificationType.SUCCESS);
                checkSessionCredentials();
            }
        }));
        DialogUtils.linkEnterToButton(loginButton, awsAccessKeyTextBox, awsSecretPasswordTextBox, mfaTokenCodeTextBox);
        checkSessionCredentials();
    }
    
    public void addListener(AwsMfaLoginListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public boolean hasValidSessionCredentials() {
        return hasValidSessionCredentials;
    }

    private void setHasValidSessionCredentials(boolean validSessionCredentials) {
        final boolean oldHasValidSessionCredentials = hasValidSessionCredentials;
        hasValidSessionCredentials = validSessionCredentials;
        awsCredentialsGrid.setVisible(!validSessionCredentials);
        if (oldHasValidSessionCredentials != validSessionCredentials) {
            listeners.forEach(l->l.awsSessionStatusChanged(validSessionCredentials));
        }
    }

    private void checkSessionCredentials() {
        landscapeManagementService.hasValidSessionCredentials(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                setHasValidSessionCredentials(false);
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(Boolean hasValidCredentials) {
                setHasValidSessionCredentials(hasValidCredentials);
            }
        });
    }
}
