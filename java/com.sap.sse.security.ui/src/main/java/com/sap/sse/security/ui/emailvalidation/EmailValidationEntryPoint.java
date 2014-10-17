package com.sap.sse.security.ui.emailvalidation;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class EmailValidationEntryPoint implements EntryPoint {
    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    private final StringMessages stringMessages = GWT.create(StringMessages.class);

    @Override
    public void onModuleLoad() {
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                RemoteServiceMappingConstants.WEB_CONTEXT_PATH,
                RemoteServiceMappingConstants.userManagementServiceRemotePath);
        final String username = Window.Location.getParameter("u");
        final String validationSecret = Window.Location.getParameter("v");
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        final Label resultLabel = new Label();
        userManagementService.validateEmail(username, validationSecret, new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                resultLabel.setText(stringMessages.errorValidatingEmail(username, caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                resultLabel.setText(stringMessages.emailValidatedSuccessfully(username));
            }
        }));
        rootPanel.add(resultLabel);
    }
}
