package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.AwsShardDTO;
import com.sap.sailing.landscape.ui.shared.LeaderboardNameDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.TableWrapperWithMultiSelectionAndFilter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.security.ui.client.component.SelectedElementsCountingButton;

public class ShardManagementPanel extends SimplePanel {
    private final ErrorReporter errorReporter;
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithMultiSelectionAndFilter<LeaderboardNameDTO, StringMessages, AdminConsoleTableResources> regattasTable;
    private final TableWrapperWithMultiSelectionAndFilter<AwsShardDTO, StringMessages, AdminConsoleTableResources> shardTable;
    private final TableWrapperWithMultiSelectionAndFilter<LeaderboardNameDTO, StringMessages, AdminConsoleTableResources> selectedKeysTable;
    private final TextBox bearertokenText;
    private SailingApplicationReplicaSetDTO<String> replicaSet;
    private final BusyIndicator shardBusy;
    private final BusyIndicator leaderboardsBusy;
    private String region;
    private String passphrase;
    private final DialogBox messageBox;
    private final TextArea message;
    private List<LeaderboardNameDTO> leaderboards;
    private Map<AwsShardDTO, Iterable<String>> shardsAndShardingKeys;
    private final Button addShard;
    private final CaptionPanel leaderboardCaption, shardsCaption, keysCaption;
    private final Button addButton, deleteButton;
    private final SelectedElementsCountingButton<AwsShardDTO> removeShardButton;
    private final DialogBox parentDialog;
    private final StringMessages stringMessages;

    public ShardManagementPanel(LandscapeManagementWriteServiceAsync pLandscapeManagementService,
            ErrorReporter errorReporter, StringMessages stringMessages, DialogBox parentDialog) {
        this.stringMessages = stringMessages;
        final VerticalPanel mainPanel = new VerticalPanel();
        this.parentDialog = parentDialog;
        mainPanel.setWidth("100%");
        add(mainPanel);
        this.errorReporter = errorReporter;
        landscapeManagementService = pLandscapeManagementService;
        final HorizontalPanel actionPanel = new HorizontalPanel();
        final Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(event -> refresh());
        actionPanel.add(refreshButton);
        regattasTable = new TableWrapperWithMultiSelectionAndFilter<LeaderboardNameDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, false, java.util.Optional.empty(),
                GWT.create(AdminConsoleTableResources.class), java.util.Optional.empty(), java.util.Optional.empty(),
                null) {
            @Override
            protected Iterable<String> getSearchableStrings(LeaderboardNameDTO t) {
                Set<String> res = new HashSet<String>();
                res.add(t.getName());
                return res;
            }
        };
        addShard = new SelectedElementsCountingButton<LeaderboardNameDTO>(stringMessages.addShard(),
                regattasTable.getSelectionModel(), e -> removeButtonPress());
        actionPanel.add(addShard);
        addShard.addClickHandler(event -> {
            addShardButtonPress();
        });
        shardTable = new TableWrapperWithMultiSelectionAndFilter<AwsShardDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, false, java.util.Optional.empty(),
                GWT.create(AdminConsoleTableResources.class), java.util.Optional.empty(), java.util.Optional.empty(),
                null) {
            @Override
            protected Iterable<String> getSearchableStrings(AwsShardDTO t) {
                final Set<String> res = new HashSet<String>();
                res.add(t.getName());
                return res;
            }
        };
        removeShardButton = new SelectedElementsCountingButton<AwsShardDTO>(stringMessages.remove(),
                shardTable.getSelectionModel(), /* element name mapper */ rs -> rs.getName(),
                stringMessages::doYouReallyWantToRemoveSelectedElements, e -> removeButtonPress());
        actionPanel.add(removeShardButton);
        mainPanel.add(actionPanel);
        final HorizontalPanel usercredentials = new HorizontalPanel();
        bearertokenText = new TextBox();
        final Label brearerhinttext = new Label(stringMessages.bearerTokenOrNullForApplicationReplicaSetToArchive(""));
        brearerhinttext.getElement().getStyle().setFontSize(15, Unit.PX);
        usercredentials.add(brearerhinttext);
        usercredentials.add(bearertokenText);
        mainPanel.add(usercredentials);
        shardsCaption = new CaptionPanel(stringMessages.shard());
        shardTable.addColumn(t -> t.getName(), stringMessages.shardname());
        shardTable.addColumn(t -> String.join(", ", t.getLeaderboardNames()), stringMessages.shardingKeys());
        final SafeHtmlCell targetgroupCell = new SafeHtmlCell();
        final Column<AwsShardDTO, SafeHtml> targetgroupColumn = new Column<AwsShardDTO, SafeHtml>(targetgroupCell) {
            @Override
            public SafeHtml getValue(AwsShardDTO shard) {
                return new LinkBuilder().setTargetgroupName(shard.getTargetgroupName()).setRegion(region)
                        .setPathMode(LinkBuilder.pathModes.TargetgroupSearch).build();
            }
        };
        shardTable.addColumn(targetgroupColumn, stringMessages.Targetgroup());
        shardTable.addColumn(t -> t.getAutoscalingGroupName(), stringMessages.Autoscalinggroup());
        shardTable.getSelectionModel().addSelectionChangeHandler(event -> {
            updateRemoveShardButton();
            updateSelectedKeysTable();
            updateAddDeleteButton();
        });
        updateRemoveShardButton();
        leaderboardCaption = new CaptionPanel(stringMessages.leaderboards());
        regattasTable.addColumn(t -> t.getName(), stringMessages.leaderboards());
        regattasTable.getSelectionModel().addSelectionChangeHandler(event -> {
            updateAddDeleteButton();
        });
        leaderboardCaption.add(regattasTable);
        final HorizontalPanel tableRow = new HorizontalPanel();
        leaderboardsBusy = new SimpleBusyIndicator();
        shardBusy = new SimpleBusyIndicator();
        keysCaption = new CaptionPanel(stringMessages.keys());
        selectedKeysTable = new TableWrapperWithMultiSelectionAndFilter<LeaderboardNameDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, false, java.util.Optional.empty(),
                GWT.create(AdminConsoleTableResources.class), java.util.Optional.empty(), java.util.Optional.empty(),
                null) {
            @Override
            protected Iterable<String> getSearchableStrings(LeaderboardNameDTO t) {
                Set<String> result = new HashSet<>();
                if (t != null && !t.getName().isEmpty()) {
                    result.add(t.getName());
                }
                return result;
            }
        };
        selectedKeysTable.addColumn(t -> t.getName(), stringMessages.keys());
        selectedKeysTable.getSelectionModel().addSelectionChangeHandler(event -> {
            updateAddDeleteButton();
            updateRemoveShardButton();
        });
        keysCaption.setVisible(false);
        keysCaption.add(selectedKeysTable);
        addButton = new Button("<");
        addButton.addClickHandler(event -> addLeaderboardsToShard());
        deleteButton = new Button(">");
        deleteButton.addClickHandler(event -> removeLeaderboardsFromShard());
        final HorizontalPanel insideShardPanel = new HorizontalPanel();
        final VerticalPanel buttonPanel = new VerticalPanel();
        final SimplePanel spaceholder = new SimplePanel();
        spaceholder.setHeight("60px");
        final SimplePanel keysSpaceholder = new SimplePanel();
        buttonPanel.add(spaceholder);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        tableRow.add(shardsCaption);
        tableRow.add(shardBusy);
        tableRow.add(leaderboardsBusy);
        insideShardPanel.add(shardTable);
        insideShardPanel.add(keysSpaceholder);
        keysSpaceholder.add(keysCaption);
        insideShardPanel.add(buttonPanel);
        insideShardPanel.add(leaderboardCaption);
        shardsCaption.add(insideShardPanel);
        shardsCaption.setWidth("90%");
        messageBox = new DialogBox(false);
        final VerticalPanel dialogMainPanel = new VerticalPanel();
        dialogMainPanel.setSpacing(5);
        messageBox.add(dialogMainPanel);
        message = new TextArea();
        message.setPixelSize(200, 300);
        dialogMainPanel.add(message);
        Button closeButton = new Button(stringMessages.close());
        closeButton.addClickHandler(event -> messageBox.hide());
        dialogMainPanel.add(closeButton);
        mainPanel.add(tableRow);
        messageBox.center();
        messageBox.hide();
        updateAddDeleteButton();
    }

    private void updateSelectedKeysTable() {
        if (shardTable.getSelectionModel().getSelectedSet().size() == 1) {
            keysCaption.setVisible(true);
            selectedKeysTable
                    .refresh(Util.map(shardTable.getSelectionModel().getSelectedSet().iterator().next().getLeaderboardNames(),
                            t -> new LeaderboardNameDTO(t)));
        } else {
            keysCaption.setVisible(false);
        }
    }

    private void updateRemoveShardButton() {
        removeShardButton.setEnabled(!shardTable.getSelectionModel().getSelectedSet().isEmpty()
                && selectedKeysTable.getSelectionModel().getSelectedSet().size() == 0);
    }

    private void updateAddDeleteButton() {
        addButton.setEnabled(shardTable.getSelectionModel().getSelectedSet().size() == 1
                && regattasTable.getSelectionModel().getSelectedSet().size() > 0);
        deleteButton.setEnabled(shardTable.getSelectionModel().getSelectedSet().size() == 1
                && selectedKeysTable.getSelectionModel().getSelectedSet().size() > 0);
    }

    private void appendMessage(String msg) {
        message.setText(message.getText() + '\n' + msg);
        messageBox.show();
    }

    public void refresh() {
        setLeaderboardBusy(true);
        setShardTableBusy(true);
        getLeaderboards();
    }

    private void display() {
        final List<String> takenLeaderboardNames = new ArrayList<>();
        for (Entry<AwsShardDTO, Iterable<String>> s : shardsAndShardingKeys.entrySet()) {
            for (String leaderboardName : s.getKey().getLeaderboardNames()) {
                takenLeaderboardNames.add(leaderboardName);
            }
        }
        final Iterable<LeaderboardNameDTO> leaderboardsToDisplay = Util.filter(leaderboards,
                leaderboardNameDTO -> !takenLeaderboardNames.contains(leaderboardNameDTO.getName()));
        shardsCaption.setVisible(true);
        regattasTable.refresh(leaderboardsToDisplay);
        shardTable.refresh(shardsAndShardingKeys.keySet());
        if (parentDialog != null) {
            parentDialog.center();
        }
    }

    private void setLeaderboardBusy(boolean b) {
        leaderboardCaption.setVisible(!b);
        leaderboardsBusy.setBusy(b);
    }

    private void setShardTableBusy(boolean b) {
        shardsCaption.setVisible(!b);
        keysCaption.setVisible(false);
        shardBusy.setBusy(b);
    }

    private void getLeaderboards() {
        setLeaderboardBusy(true);
        landscapeManagementService.getLeaderboardNames(replicaSet, getBearerToken(),
                new AsyncCallback<ArrayList<LeaderboardNameDTO>>() {
                    @Override
                    public void onSuccess(ArrayList<LeaderboardNameDTO> result) {
                        leaderboards = result;
                        setLeaderboardBusy(false);
                        getShards();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error while retriving regattas.");
                        setLeaderboardBusy(false);
                        setShardTableBusy(false);
                    }
                });
    }

    private String getBearerToken() {
        return Util.hasLength(bearertokenText.getValue()) ? bearertokenText.getValue(): null;
    }

    private void getShards() {
        landscapeManagementService.getShards(replicaSet, region, getBearerToken(),
                new AsyncCallback<Map<AwsShardDTO, Iterable<String>>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                        setShardTableBusy(false);
                    }

                    @Override
                    public void onSuccess(Map<AwsShardDTO, Iterable<String>> result) {
                        shardsAndShardingKeys = result;
                        setShardTableBusy(false);
                        display();
                    }
                });
    }

    private void addShardButtonPress() {
        final Set<LeaderboardNameDTO> selectedLeaderboards = regattasTable.getSelectionModel().getSelectedSet();
        if (!selectedLeaderboards.isEmpty() && replicaSet != null) {
            final DataEntryDialog<String> nameRequest = new DataEntryDialog<String>(
                    stringMessages.shardname(), stringMessages.enterShardName(), stringMessages.ok(), stringMessages.cancel(),
                    new Validator<String>() {
                        @Override
                        public String getErrorMessage(String valueToValidate) {
                            final String errorMessage;
                            if (!Util.hasLength(valueToValidate)) {
                                errorMessage = stringMessages.pleaseProvideANonEmptyShardName();
                            } else {
                                errorMessage = null;
                            }
                            return errorMessage;
                        }
                }, new DialogCallback<String>() {
                        @Override
                        public void ok(String newShardName) {
                            ArrayList<LeaderboardNameDTO> l = new ArrayList<>();
                            l.addAll(selectedLeaderboards);
                            landscapeManagementService.addShard(newShardName, l, replicaSet,
                                    getBearerToken(), region, passphrase.getBytes(), new AsyncCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            Notification.notify("Created succesfully", NotificationType.SUCCESS);
                                            refresh();
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError(caught.getMessage());
                                        }
                                    });
                        }

                        @Override
                        public void cancel() {
                        }
                    }) {
                private final TextBox nameTextBox = createTextBox("", /* length */ 20);
                
                @Override
                protected Widget getAdditionalWidget() {
                    return nameTextBox;
                }

                @Override
                protected String getResult() {
                    return nameTextBox.getText();
                }
            };
            nameRequest.show();
        }
    }

    private void removeButtonPress() {
        setShardTableBusy(true);
        for (AwsShardDTO selection : shardTable.getSelectionModel().getSelectedSet()) {
            landscapeManagementService.removeShard(selection, replicaSet, region, passphrase.getBytes(),
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(caught.getMessage());
                            setShardTableBusy(false);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            appendMessage("Deleted Shard: " + selection.getName());
                            refresh();
                        }
                    });
        }

    }

    private void addLeaderboardsToShard() {
        setShardTableBusy(true);
        if (!regattasTable.getSelectionModel().getSelectedSet().isEmpty()
                && shardTable.getSelectionModel().getSelectedSet().size() == 1) {
            final Iterable<LeaderboardNameDTO> selectedLeaderboards = regattasTable.getSelectionModel().getSelectedSet();
            final AwsShardDTO shard = shardTable.getSelectionModel().getSelectedSet().iterator().next();
            landscapeManagementService.appendShardingKeysToShard(selectedLeaderboards, region, shard.getName(),
                    replicaSet, getBearerToken(), passphrase.getBytes(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            setLeaderboardBusy(false);
                            errorReporter.reportError(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            setLeaderboardBusy(false);
                            appendMessage(stringMessages.successfullyAppendedShardingKeysToShard(Util.join(", ", selectedLeaderboards), shard.getName()));
                            refresh();
                        }
                    });
        } else {
        }
    }

    private void removeLeaderboardsFromShard() {
        setShardTableBusy(true);
        if (!selectedKeysTable.getSelectionModel().getSelectedSet().isEmpty()
                && shardTable.getSelectionModel().getSelectedSet().size() == 1) {
            final Iterable<LeaderboardNameDTO> selectedLeaderboards = selectedKeysTable.getSelectionModel().getSelectedSet();
            final AwsShardDTO shard = shardTable.getSelectionModel().getSelectedSet().iterator().next();
            landscapeManagementService.removeShardingKeysFromShard(selectedLeaderboards, region, shard.getName(),
                    replicaSet, getBearerToken(), passphrase.getBytes(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            setLeaderboardBusy(false);
                            errorReporter.reportError(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            setLeaderboardBusy(false);
                            appendMessage(stringMessages.successfullyRemovedLeaderboardsFromShard(Util.join(", ", selectedLeaderboards), shard.getName()));
                            refresh();
                        }
                    });
        }
    }

    public void setReplicaSet(SailingApplicationReplicaSetDTO<String> replicaset) {
        replicaSet = replicaset;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

}
