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
import com.sap.sse.common.Util;
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
    private final TextBox sessionTokenTextBox;
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
        awsCredentialsGrid = new Grid(5, 2);
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
        int row=0;
        awsCredentialsGrid.setWidget(row++, 1, awsAccessKeyTextBox);
        awsCredentialsGrid.setWidget(row, 0, new Label(stringMessages.awsSecret()));
        awsSecretPasswordTextBox = new PasswordTextBox();
        awsCredentialsGrid.setWidget(row++, 1, awsSecretPasswordTextBox);
        mfaTokenCodeTextBox = new TextBox();
        awsCredentialsGrid.setWidget(row, 0, new Label(stringMessages.mfaTokenCode()));
        awsCredentialsGrid.setWidget(row++, 1, mfaTokenCodeTextBox);
        sessionTokenTextBox = new TextBox();
        awsCredentialsGrid.setWidget(row, 0, new Label(stringMessages.optionalSessionToken()));
        awsCredentialsGrid.setWidget(row++, 1, sessionTokenTextBox);
        final Button loginButton = new Button(stringMessages.login());
        awsCredentialsGrid.setWidget(row++, 0, loginButton);
        awsCredentialsGrid.setVisible(false);
        refreshButton.addClickHandler(e->checkSessionCredentials());
        final AsyncCallback<Void> sessionCredentialsCallback = new AsyncCallback<Void>() {
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
        };
        loginButton.addClickHandler(e->{
            if (Util.hasLength(mfaTokenCodeTextBox.getValue())) {
                landscapeManagementService.createMfaSessionCredentials(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(), mfaTokenCodeTextBox.getValue(),
                        sessionCredentialsCallback);
            } else {
                landscapeManagementService.createSessionCredentials(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(), sessionTokenTextBox.getValue(),
                        sessionCredentialsCallback);
            }
        });
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
    
    @Override
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
