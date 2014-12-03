package com.sap.sse.security.ui.emailvalidation;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;

public class EmailValidationEntryPoint extends AbstractSecurityEntryPoint {
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        final String username = Window.Location.getParameter("u");
        final String validationSecret = Window.Location.getParameter("v");
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        final Label resultLabel = new Label();
        getUserManagementService().validateEmail(username, validationSecret, new MarkedAsyncCallback<Boolean>(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                resultLabel.setText(getStringMessages().errorValidatingEmail(username, caught.getMessage()));
            }

            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    getUserService().updateUser(/* notifyOtherInstances */ true);
                    resultLabel.setText(getStringMessages().emailValidatedSuccessfully(username));
                } else {
                    resultLabel.setText(getStringMessages().emailValidationUnsuccessful(username));
                }
            }
        }));
        rootPanel.add(resultLabel);
    }
}
