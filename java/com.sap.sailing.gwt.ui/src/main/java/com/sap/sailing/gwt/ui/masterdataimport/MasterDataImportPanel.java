package com.sap.sailing.gwt.ui.masterdataimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.MediaTracksRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.controls.progressbar.CustomProgressBar;

public class MasterDataImportPanel extends VerticalPanel {

    private ListBox leaderboardgroupListBox;
    private TextBox hostBox;
    private Button importLeaderboardGroupsButton;
    private Button fetchIdsButton;   

    private List<String> allLeaderboardGroupNames;

    private final StringMessages stringMessages;
    private String currentHost;
    private SailingServiceAsync sailingService;
    private CheckBox overrideSwitch;
    private final RegattaRefresher regattaRefresher;
    private final EventsRefresher eventRefresher;
    private final LeaderboardsRefresher leaderboardsRefresher;
    private final LeaderboardGroupsRefresher leaderboardGroupsRefresher;
    private final MediaTracksRefresher mediaTracksRefresher;
    private CheckBox compressSwitch;
    private CheckBox exportWindSwitch;
    private CheckBox exportDeviceConfigsSwitch;
    private TextBox filterBox;
    private TextBox usernameBox;
    private TextBox passwordBox;

    public MasterDataImportPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            RegattaRefresher regattaRefresher, EventsRefresher eventsRefresher, LeaderboardsRefresher leaderboardsRefresher,
            LeaderboardGroupsRefresher leaderboardGroupsRefresher, MediaTracksRefresher mediaTracksRefresher) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.regattaRefresher = regattaRefresher;
        this.eventRefresher = eventsRefresher;
        this.leaderboardsRefresher = leaderboardsRefresher;
        this.leaderboardGroupsRefresher = leaderboardGroupsRefresher;
        this.mediaTracksRefresher = mediaTracksRefresher;

        HorizontalPanel serverAddressPanel = new HorizontalPanel();
        serverAddressPanel.add(new Label(stringMessages.importRemoteHost()));
        hostBox = new TextBox();
        hostBox.setText("https://www.sapsailing.com/");
        hostBox.setWidth("300px");
        serverAddressPanel.add(hostBox);
        HorizontalPanel usernamePanel = new HorizontalPanel();
        usernamePanel.add(new Label(stringMessages.username()));
        usernameBox = new TextBox();
        usernameBox.setText("admin");
        usernameBox.setWidth("300px");
        usernamePanel.add(usernameBox);
        HorizontalPanel passwordPanel = new HorizontalPanel();
        passwordPanel.add(new Label(stringMessages.password()));
        passwordBox = new PasswordTextBox();
        passwordBox.setText("");
        passwordBox.setWidth("300px");
        passwordPanel.add(passwordBox);
        fetchIdsButton = new Button(stringMessages.importFetchRemoteLgs());
        fetchIdsButton.ensureDebugId("fetchLeaderboardGroupList");


        this.add(serverAddressPanel);
        this.add(usernamePanel);
        this.add(passwordPanel);
        this.add(fetchIdsButton);

        ScrollPanel scrollPanel = new ScrollPanel();
        this.add(scrollPanel);

        VerticalPanel contentPanel = new VerticalPanel();
        scrollPanel.setWidget(contentPanel);

        addContentToLeftPanel(contentPanel);

        setListeners();
    }

    private void setListeners() {
        fetchIdsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                fireIdRequestsAndFillLists();
            }
        });

        hostBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    fireIdRequestsAndFillLists();
                }
            }
        });

        importLeaderboardGroupsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                importLeaderboardGroups();
            }
        });
    }

    protected void importLeaderboardGroups() {
        String[] groupNames = createLeaderBoardGroupNamesFromListBox();
        final Label overallName = new Label(stringMessages.overallProgress() + ":");
        this.add(overallName);
        final CustomProgressBar overallProgressBar = CustomProgressBar.determinate();
        overallProgressBar.ensureDebugId("overallProgressBar");
        this.add(overallProgressBar);
        final Label subProgressName = new Label();
        this.add(subProgressName);
        final CustomProgressBar subProgressBar = CustomProgressBar.determinate();
        this.add(subProgressBar);
        if (groupNames.length >= 1) {
            disableAllButtons();
            boolean override = overrideSwitch.getValue();
            boolean compress = compressSwitch.getValue();
            boolean exportWind = exportWindSwitch.getValue();
            boolean exportDeviceConfigs = exportDeviceConfigsSwitch.getValue();
            sailingService.importMasterData(currentHost, groupNames, override, compress, exportWind,
                    exportDeviceConfigs, usernameBox.getValue(), passwordBox.getValue(), new AsyncCallback<UUID>() {

                @Override
                public void onFailure(Throwable caught) {
                    showErrorAlert(caught.getLocalizedMessage());
                }

                @Override
                public void onSuccess(final UUID resultId) {
                    final Timer timer = new Timer() {

                        @Override
                        public void run() {
                            sailingService.getImportOperationProgress(resultId,
                                    new AsyncCallback<DataImportProgress>() {

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            showErrorAlert(stringMessages.importServerError());
                                        }

                                        @Override
                                        public void onSuccess(DataImportProgress result) {
                                            if (result == null) {
                                                showErrorAlert(stringMessages.importServerError());
                                            } else {
                                                if (result.failed()) {
                                                    // Got error, cancel timer
                                                    cancel();
                                                    deleteProgressIndication(overallName, overallProgressBar,
                                                            subProgressName, subProgressBar);

                                                    showErrorAlert(result.getErrorMessage());
                                                    changeButtonStateAccordingToApplicationState();
                                                } else {
                                                    MasterDataImportObjectCreationCount creationCount = result
                                                            .getResult();
                                                    if (creationCount != null) {
                                                        // Got result, cancel timer
                                                        cancel();
                                                        deleteProgressIndication(overallName, overallProgressBar,
                                                                subProgressName, subProgressBar);
                                                        showCreationMessage(creationCount);
                                                    } else {
                                                        overallProgressBar.setValue(result.getOverallProgressPct());
                                                        subProgressName.setText(result.getCurrentSubProgress()
                                                                .getMessage(stringMessages));
                                                        subProgressBar.setValue(result.getCurrentSubProgressPct());
                                                    }
                                                }
                                            }
                                        }
                                    });
                        }
                    };
                    timer.scheduleRepeating(5000);
                }
            });
        } else {
            showErrorAlert(stringMessages.importSelectAtLeastOne());
        }
    }

    private void showCreationMessage(MasterDataImportObjectCreationCount creationCount) {
        int leaderboardsCreated = creationCount.getLeaderboardCount();
        int leaderboardGroupsCreated = creationCount.getLeaderboardGroupCount();
        int eventsCreated = creationCount.getEventCount();
        int regattasCreated = creationCount.getRegattaCount();
        int mediaTracksImported = creationCount.getMediaTrackCount();
        if (regattasCreated > 0) {
            regattaRefresher.fillRegattas();
        }
        if (eventsCreated > 0) {
            eventRefresher.fillEvents();
        }
        if (leaderboardGroupsCreated > 0) {
            leaderboardGroupsRefresher.fillLeaderboardGroups();
        }
        if (leaderboardsCreated > 0) {
            leaderboardsRefresher.fillLeaderboards();
        }
        if (mediaTracksImported > 0) {
        	mediaTracksRefresher.loadMediaTracks();
        }
        Set<String> overwrittenRegattas = creationCount.getOverwrittenRegattaNames();
        showSuccessAlert(leaderboardsCreated, leaderboardGroupsCreated, eventsCreated, regattasCreated,
                mediaTracksImported, overwrittenRegattas);
        changeButtonStateAccordingToApplicationState();
    }

    private void disableAllButtons() {
        boolean enabled = false;
        importLeaderboardGroupsButton.setEnabled(enabled);
        overrideSwitch.setEnabled(enabled);
        fetchIdsButton.setEnabled(enabled);
    }
    
    private void changeButtonStateAccordingToApplicationState() {
        if (leaderboardgroupListBox.getItemCount() > 0 && countSelectedItems() > 0) {
            importLeaderboardGroupsButton.setEnabled(true);
        } else {
            importLeaderboardGroupsButton.setEnabled(false);
        }
        overrideSwitch.setEnabled(true);
        fetchIdsButton.setEnabled(true);
    }


    private int countSelectedItems() {
        int count = 0;
        for (int i = 0; i < leaderboardgroupListBox.getItemCount(); i++) {
            if (leaderboardgroupListBox.isItemSelected(i)) {
                count++;
            }
        }
        return count;
    }

    protected void showSuccessAlert(int leaderboardsCreated, int leaderboardGroupsCreated, int eventsCreated,
            int regattasCreated, int mediaTracksImported, Set<String> overwrittenRegattas) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(stringMessages.importSuccess(leaderboardGroupsCreated, leaderboardsCreated, eventsCreated,
                regattasCreated, mediaTracksImported));
        if (overwrittenRegattas.size() > 0) {
            buffer.append("\n\n" + stringMessages.importSuccessOverwriteInfo() + "\n");
            for (String regattaName : overwrittenRegattas) {
                buffer.append(regattaName + "\n");
            }
        }
        Notification.notify(buffer.toString(), NotificationType.SUCCESS);

    }

    private String[] createLeaderBoardGroupNamesFromListBox() {
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < leaderboardgroupListBox.getItemCount(); i++) {
            if (leaderboardgroupListBox.isItemSelected(i)) {
                names.add(leaderboardgroupListBox.getValue(i));
            }
        }
        return names.toArray(new String[names.size()]);
    }

    protected void fireIdRequestsAndFillLists() {
        String host = hostBox.getText();
        if (host != null && !host.isEmpty()) {
            fireLgIdRequestAndFillList(host);
        }
    }

    private void fireLgIdRequestAndFillList(final String host) {
        currentHost = host;
        disableAllButtons();
        // FIXME what about login here?
        sailingService.getLeaderboardGroupNamesFromRemoteServer(host, usernameBox.getValue(), passwordBox.getValue(),
                new AsyncCallback<List<String>>() {

            @Override
            public void onFailure(Throwable caught) {
                showErrorAlert(stringMessages.importGetLeaderboardsFailed(host, caught.getMessage()));
                changeButtonStateAccordingToApplicationState();
            }

            @Override
            public void onSuccess(List<String> result) {
                clearListBox();
                Collections.sort(result);
                allLeaderboardGroupNames = result;
                leaderboardgroupListBox.setVisibleItemCount(result.size());
                for (String lgName : result) {
                    leaderboardgroupListBox.addItem(lgName);
                }
                changeButtonStateAccordingToApplicationState();
                if (!filterBox.getValue().isEmpty()) {
                    filterLeaderboardGroupList();
                }
            }

        });
    }

    private void showErrorAlert(String string) {
        Notification.notify(string, NotificationType.ERROR);
    }

    private void addContentToLeftPanel(VerticalPanel contentPanel) {
        contentPanel.add(new Label(stringMessages.availableLeaderboardGroups()));
        
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.add(new Label(stringMessages.filterName() + ":"));
        filterBox = new TextBox();
        setFilterHandler(filterBox);
        filterPanel.add(filterBox);
        contentPanel.add(filterPanel);

        leaderboardgroupListBox = new ListBox();
        leaderboardgroupListBox.ensureDebugId("LeaderBoardGroupListBox");
        leaderboardgroupListBox.setMultipleSelect(true);

        addSelectionChangedListener();
        contentPanel.add(leaderboardgroupListBox);
        
        overrideSwitch = new CheckBox(stringMessages.importOverrideSwitchLabel());
        overrideSwitch.ensureDebugId("overrideExisting");
        overrideSwitch.setValue(false);
        contentPanel.add(overrideSwitch);
        
        compressSwitch = new CheckBox(stringMessages.compress());
        compressSwitch.setTitle(stringMessages.compressTooltip());
        compressSwitch.setValue(true);
        contentPanel.add(compressSwitch);

        exportWindSwitch = new CheckBox(stringMessages.importWind());
        exportWindSwitch.setTitle(stringMessages.importWindTooltip());
        exportWindSwitch.setValue(true);
        exportWindSwitch.ensureDebugId("wind");
        contentPanel.add(exportWindSwitch);
        
        exportDeviceConfigsSwitch = new CheckBox(stringMessages.importDeviceConfigurations());
        exportDeviceConfigsSwitch.setTitle(stringMessages.importDeviceConfigurationsTooltip());
        exportDeviceConfigsSwitch.setValue(false);
        contentPanel.add(exportDeviceConfigsSwitch);

        importLeaderboardGroupsButton = new Button(stringMessages.importSelectedLeaderboardGroups());
        importLeaderboardGroupsButton.ensureDebugId("import");
        importLeaderboardGroupsButton.setEnabled(false);
        contentPanel.add(importLeaderboardGroupsButton);
    }

    private void addSelectionChangedListener() {
        leaderboardgroupListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                changeButtonStateAccordingToApplicationState();
            }
        });
    }

    private void setFilterHandler(final TextBox filterBox) {
        filterBox.addKeyUpHandler(new KeyUpHandler() {
            
            @Override
            public void onKeyUp(KeyUpEvent event) {
                filterLeaderboardGroupList();
            }
        });
    }

    public void clearListBox() {
        int itemCount = leaderboardgroupListBox.getItemCount();
        for (int i = itemCount - 1; i >= 0; i--) {
            leaderboardgroupListBox.removeItem(i);
        }
    }

    private void filterLeaderboardGroupList() {
        clearListBox();
        int visibleNameCount = 0;
        List<String> filterTexts = Arrays.asList(filterBox.getText().split(" "));
        for (String name : allLeaderboardGroupNames) {
            boolean containsAllFilterTexts = true;
            for (String filterText : filterTexts) {
                if (!name.toUpperCase().contains(filterText.toUpperCase())) {
                    containsAllFilterTexts = false;
                    break;
                }
            }
            if (containsAllFilterTexts) {
                leaderboardgroupListBox.addItem(name);
                visibleNameCount++;
            }
        }
        leaderboardgroupListBox.setVisibleItemCount(visibleNameCount);
        changeButtonStateAccordingToApplicationState();
    }

    private void deleteProgressIndication(IsWidget... widgetsToRemove) {
        Arrays.asList(widgetsToRemove).forEach(this::remove);
    }

}
