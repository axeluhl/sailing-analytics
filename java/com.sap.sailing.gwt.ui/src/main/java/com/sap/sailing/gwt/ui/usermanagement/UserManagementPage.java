package com.sap.sailing.gwt.ui.usermanagement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.Base64Utils;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.UserManagementService;
import com.sap.sailing.gwt.ui.client.UserManagementServiceAsync;
import com.sap.sse.gwt.client.dialog.DialogUtils;

public class UserManagementPage extends AbstractEntryPoint {
    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        registerASyncService((ServiceDefTarget) userManagementService, RemoteServiceMappingConstants.userManagementServiceRemotePath);

        VerticalPanel vp = new VerticalPanel();
        RootPanel.get().add(vp);
        TextBox usernameField = new TextBox();
        vp.add(usernameField);
        final PasswordTextBox passwordField = new PasswordTextBox();
        vp.add(passwordField);
        final Label md5Display = new Label();
        vp.add(md5Display);
        Button ok = new Button("OK");
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String password = passwordField.getValue();
                try {
                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    byte[] md5 = digest.digest(password.getBytes());
                    md5Display.setText(Base64Utils.toBase64(md5));
                } catch (NoSuchAlgorithmException e) {
                    reportError("Error creating digest: "+e.getMessage());
                }
            }
        });
        vp.add(ok);
        DialogUtils.linkEnterToButton(ok, usernameField, passwordField);
    }
}