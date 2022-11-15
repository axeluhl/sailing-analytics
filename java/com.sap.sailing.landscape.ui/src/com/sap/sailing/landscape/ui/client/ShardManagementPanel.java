package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.TableWrapperWithMultiSelectionAndFilter;

public class ShardManagementPanel extends SimplePanel {
    private final ErrorReporter errorReporter;
    private final Button testButton;
    private final Button addShard;
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithMultiSelectionAndFilter<String, StringMessages, AdminConsoleTableResources> regattasTable;
    private final PasswordTextBox passwordText;
    private final TextBox usernameText;
    private SailingApplicationReplicaSetDTO<String> replicaSet;
    private String region;
    private String passphrase;
    public ShardManagementPanel(LandscapeManagementWriteServiceAsync pLandscapeManagementService,ErrorReporter errorReporter, StringMessages stringMessages) {
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        add(mainPanel);
        this.errorReporter = errorReporter;
        landscapeManagementService = pLandscapeManagementService;
        testButton = new Button("Test Button -  Displays leaderboards");
        mainPanel.add(testButton);
        final HorizontalPanel usercredentials = new HorizontalPanel();
        passwordText = new PasswordTextBox();
        usernameText = new TextBox();
        usernameText.setText("niklasopiela");
        usercredentials.add(new Label("Username: "));
        usercredentials.add(usernameText);
        usercredentials.add(new Label("Password"));
        usercredentials.add(passwordText);  
        mainPanel.add(usercredentials);  
        addShard = new Button("Add Shard");
        mainPanel.add(addShard);
        addShard.addClickHandler(event -> {
            addShardButtonPress();
        });
        testButton.addClickHandler(event -> {
            landscapeManagementService.getLeaderboardNames(replicaSet, usernameText.getValue(),passwordText.getValue(),  new AsyncCallback<ArrayList<String>>() {
               
                @Override
                public void onSuccess(ArrayList<String> result) {
                    result.forEach(t -> regattasTable.add(t));
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error while retriving regattas.");
                    
                }
            });
        });
        regattasTable = new TableWrapperWithMultiSelectionAndFilter<String, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, false, java.util.Optional.empty(), GWT.create(AdminConsoleTableResources.class),
                java.util.Optional.empty(), java.util.Optional.empty(), null) {

            @Override
            protected Iterable<String> getSearchableStrings(String t) {
                Set<String> result = new HashSet<>();
                if(t != null && !t.isEmpty()) {
                    result.add(t);
                }
                return result;
            }
        };
        regattasTable.addColumn(t -> t, "Leaderboards");
        mainPanel.add(regattasTable);   
    }

    
    private void addShardButtonPress() {
        Set<String> selectedLeaderboards = regattasTable.getSelectionModel().getSelectedSet();
        if (!selectedLeaderboards.isEmpty() && replicaSet != null) {
            
            DialogBox nameRequest = new DialogBox(false);
            final VerticalPanel mainPanel = new VerticalPanel();
            mainPanel.setSpacing(5);
            mainPanel.setWidth("100%");
            nameRequest.add(mainPanel);
            mainPanel.add(new Label("Bitte namen eingeben: "));
            TextBox nameText  =new TextBox();
            mainPanel.add(nameText);
            Button submitButton = new Button("Submit");
            mainPanel.add(submitButton);
            submitButton.addClickHandler(event -> {
               if(nameText.getValue() == null || nameText.getValue() == "" || region == null || passphrase == null) {
                   nameRequest.setVisible(false);
                   errorReporter.reportError("Name not valid");
               }else if(region == null || passphrase == null) {
                   nameRequest.setVisible(false);
                   errorReporter.reportError("Region and passphrase must be set");
               } 
               else {
                   nameRequest.setVisible(false);
                   landscapeManagementService.addShard(nameText.getValue(), selectedLeaderboards, replicaSet, usernameText.getValue(),
                    passwordText.getValue(), region, passphrase.getBytes(), new AsyncCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            errorReporter.reportError("Success");
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(caught.getMessage());

                        }
                    });
               }
            });
            nameRequest.center();
            nameRequest.show();
            
            
            
        }
    }
    
    public void setReplicaSet(SailingApplicationReplicaSetDTO<String> rs) {
        replicaSet = rs;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public void setPassphrase(String passphrase) {
        this.passphrase  =passphrase;
    }
    
    
    
}

