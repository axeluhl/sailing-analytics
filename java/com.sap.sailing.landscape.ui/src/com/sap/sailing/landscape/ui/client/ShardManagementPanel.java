package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.AwsShardDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.TableWrapperWithMultiSelectionAndFilter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

public class ShardManagementPanel extends SimplePanel {
    private final ErrorReporter errorReporter;
    private final Button addShard;
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithMultiSelectionAndFilter<String, StringMessages, AdminConsoleTableResources> regattasTable;
    private final TableWrapperWithMultiSelectionAndFilter< Entry<AwsShardDTO, Iterable<String>>, StringMessages, AdminConsoleTableResources> shardTable;
    private final TextBox brearertokenText;
    private SailingApplicationReplicaSetDTO<String> replicaSet;
    private final BusyIndicator shardBusy;
    private final BusyIndicator leaderboardsBusy;
    private String region;
    private String passphrase;
    private final DialogBox messageBox;
    private final TextArea message;
    public ShardManagementPanel(LandscapeManagementWriteServiceAsync pLandscapeManagementService,ErrorReporter errorReporter, StringMessages stringMessages) {
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        add(mainPanel);
        
        this.errorReporter = errorReporter;
        landscapeManagementService = pLandscapeManagementService;
        final HorizontalPanel actionPanel = new HorizontalPanel();
        final Button refreshButton  = new Button("Refresh");
        refreshButton.addClickHandler(event -> refresh());
        actionPanel.add(refreshButton);
        addShard = new Button("Add Shard");
        actionPanel.add(addShard);
        addShard.addClickHandler(event -> {
            addShardButtonPress();
        });
        final Button removeButton  =new Button("Remove");
        actionPanel.add(removeButton);
        removeButton.addClickHandler(event -> removeButtonPress());
        mainPanel.add(actionPanel);
        final HorizontalPanel usercredentials = new HorizontalPanel();
        brearertokenText = new TextBox();
        brearertokenText.setText("7heziyoyPvcy1SudfiUYx6JgL58LSNka7W1MDR2J4wo=");
        Label brearerhinttext = new Label("Bearertoken: ");
        brearerhinttext.getElement().getStyle().setFontSize(15, Unit.PX);
        usercredentials.add(brearerhinttext);
        
        usercredentials.add(brearertokenText);
 
        mainPanel.add(usercredentials);  
        
        shardTable = new TableWrapperWithMultiSelectionAndFilter<Entry<AwsShardDTO, Iterable<String>>, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, false, java.util.Optional.empty(),
                GWT.create(AdminConsoleTableResources.class), java.util.Optional.empty(), java.util.Optional.empty(),
                null) {

            @Override
            protected Iterable<String> getSearchableStrings(Entry<AwsShardDTO, Iterable<String>> t) {
                Set<String> res = new HashSet<String>();

                res.add(t.getKey().getName());
                return res;
            }
        };
        //shardTable.addColumn(t -> t.getKey().toString(),"index");
        shardTable.addColumn(t -> t.getKey().getName(),"Name");
        shardTable.addColumn(t-> t.getKey().getKeysString() , "Sharding Keys");
        final SafeHtmlCell targetgroupCell = new SafeHtmlCell();
        final Column<Entry<AwsShardDTO, Iterable<String>>, SafeHtml> targetgroupColumn = new Column<Entry<AwsShardDTO, Iterable<String>>, SafeHtml>(targetgroupCell) {
            @Override
            public SafeHtml getValue(Entry<AwsShardDTO, Iterable<String>> shardentry) {
                return new LinkBuilder().setTargetgroupName(shardentry.getKey().getTargetgroupName()).setRegion(region).setPathMode(LinkBuilder.pathModes.TargetgroupSearch).build();
            }
        };
        shardTable.addColumn(targetgroupColumn , "Targetgroup");
        shardTable.addColumn(t-> t.getKey().getAutoscalingGroupName() , "Autoscalinggroup");
        shardTable.getSelectionModel().addSelectionChangeHandler(event -> {
            removeButton.setEnabled(!shardTable.getSelectionModel().getSelectedSet().isEmpty());
        });
        removeButton.setEnabled(!shardTable.getSelectionModel().getSelectedSet().isEmpty());
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
        final HorizontalPanel tableRow  = new HorizontalPanel();
        leaderboardsBusy  =new SimpleBusyIndicator();
        shardBusy = new SimpleBusyIndicator();
        tableRow.add(regattasTable);
        tableRow.add(leaderboardsBusy);
        tableRow.add(shardTable);
        tableRow.add(shardBusy);
        
        messageBox = new DialogBox(false);
        final VerticalPanel dialogMainPanel = new VerticalPanel();
        dialogMainPanel.setSpacing(5);
        dialogMainPanel.setWidth("100%");
        messageBox.add(dialogMainPanel);
        message = new TextArea();
        message.setPixelSize(200, 300);
        dialogMainPanel.add(message);
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(event -> messageBox.hide());
        dialogMainPanel.add(closeButton);
        messageBox.center();
        messageBox.show();
        appendMessage("Started");
        appendMessage("Test");
        
        
        mainPanel.add(tableRow);   
    }
    
    private void appendMessage(String msg) {
        message.setText(message.getText() + '\n' + msg);
        messageBox.show();
    }
    
    private void refresh() {
        displayLeaderboards();
        displayShards();
    }
    
    private void setLeaderboardBusy(boolean b) {
        regattasTable.getTable().setVisible(!b);
       leaderboardsBusy.setBusy(b);
    }
    
    private void setShardtableBusy(boolean b) {
        shardTable.getTable().setVisible(!b);
       shardBusy.setBusy(b);
    }
    
    private void displayLeaderboards() {
        setLeaderboardBusy(true);
        landscapeManagementService.getLeaderboardNames(replicaSet, brearertokenText.getValue(),  new AsyncCallback<ArrayList<String>>() {
            
            @Override
            public void onSuccess(ArrayList<String> result) {
                regattasTable.refresh(result);
                setLeaderboardBusy(false);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error while retriving regattas.");
                
            }
        });
    }

    
    private void addShardButtonPress() {
        if(brearertokenText.getValue() == null || brearertokenText.getValue() == "") {
            errorReporter.reportError("Please enter a bearertoken");
            return;
        }
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
               if(nameText.getValue() == null || nameText.getValue() == "") {
                   nameRequest.setVisible(false);
                   errorReporter.reportError("Name not valid");
               }else if(region == null || passphrase == null) {
                   nameRequest.setVisible(false);
                   errorReporter.reportError("Region and passphrase must be set");
               } 
               else {
                   nameRequest.setVisible(false);
                   landscapeManagementService.addShard(nameText.getValue(), selectedLeaderboards, replicaSet, brearertokenText.getValue(),
                     region, passphrase.getBytes(), new AsyncCallback<Void>() {

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

    private void removeButtonPress() {
        setShardtableBusy(true);
        for (Entry<AwsShardDTO, Iterable<String>> selection : shardTable.getSelectionModel().getSelectedSet()) {
            landscapeManagementService.removeShard( selection.getKey(), replicaSet, region,
                    passphrase.getBytes(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(caught.getMessage());
                            setShardtableBusy(false);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            appendMessage("Deleted Shard: " + selection.getKey().getName());
                        }
                    });
        }
        refresh();
    }
    
    private void displayShards() {
        setShardtableBusy(true);
            landscapeManagementService.getShards(replicaSet, region,passphrase.getBytes(), new AsyncCallback< Map<AwsShardDTO, Iterable<String>>>() {

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                    setShardtableBusy(false);
                }

                @Override
                public void onSuccess( Map<AwsShardDTO, Iterable<String>> result) {
                    shardTable.refresh(result.entrySet());
                    setShardtableBusy(false);
                }
            });
        
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

