package com.sap.sailing.gwt.ui.masterdataimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MasterDataImportPanel extends VerticalPanel {

    private ListBox leaderboardgroupListBox;
    private TextBox hostBox;
    private Button importLeaderboardGroupsButton;
    private Button fetchIdsButton;   

    private List<String> allLeaderboardGroupNames;

    private RegExp urlValidator;
    private RegExp urlPlusTldValidator;

    private final StringMessages stringMessages;
    private String currentHost;
    private SailingServiceAsync sailingService;
    private CheckBox overrideSwitch;
    private final RegattaRefresher regattaRefresher;
    private final EventRefresher eventRefresher;
    private final LeaderboardGroupRefresher leaderboardGroupRefresher;

    public MasterDataImportPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            RegattaRefresher regattaRefresher, EventRefresher eventRefresher,
            LeaderboardGroupRefresher leaderboardGroupRefresher) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.regattaRefresher = regattaRefresher;
        this.eventRefresher = eventRefresher;
        this.leaderboardGroupRefresher = leaderboardGroupRefresher;

        HorizontalPanel serverAddressPanel = new HorizontalPanel();
        serverAddressPanel.add(new Label(stringMessages.importRemoteHost()));
        hostBox = new TextBox();
        hostBox.setText("http://live2.sapsailing.com/");
        hostBox.setWidth("300px");
        serverAddressPanel.add(hostBox);
        fetchIdsButton = new Button(stringMessages.importFetchRemoteLgs());
        serverAddressPanel.add(fetchIdsButton);
        this.add(serverAddressPanel);

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
        if (groupNames.length >= 1) {
            disableAllButtons();
            boolean override = overrideSwitch.getValue();
            sailingService.importMasterData(currentHost, groupNames, override,
                    new AsyncCallback<MasterDataImportObjectCreationCount>() {

                        @Override
                        public void onSuccess(MasterDataImportObjectCreationCount result) {
                            int leaderboardsCreated = result.getLeaderboardCount();
                            int leaderboardGroupsCreated = result.getLeaderboardGroupCount();
                            int eventsCreated = result.getEventCount();
                            int regattasCreated = result.getRegattaCount();
                            if (regattasCreated > 0) {
                                regattaRefresher.fillRegattas();
                            }
                            if (eventsCreated > 0) {
                                eventRefresher.fillEvents();
                            }
                            if (leaderboardGroupsCreated > 0) {
                                leaderboardGroupRefresher.fillLeaderboardGroups();
                            }
                            showSuccessAlert(leaderboardsCreated, leaderboardGroupsCreated, eventsCreated,
                                    regattasCreated);
                            changeButtonStateAccordingToApplicationState();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            showErrorAlert(caught.getLocalizedMessage());
                            changeButtonStateAccordingToApplicationState();
                        }
                    });
        } else {
            showErrorAlert(stringMessages.importSelectAtLeastOne());
        }
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
            int regattasCreated) {
        Window.alert(stringMessages.importSuccess(leaderboardGroupsCreated, leaderboardsCreated, eventsCreated,
                regattasCreated));

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

    private void fireLgIdRequestAndFillList(String host) {
        currentHost = host;
        final String getLgsUrl = createGetLgsUrl(host);
        if (!isValidUrl(getLgsUrl, false)) {
            showErrorAlert(stringMessages.importUrlInvalid(getLgsUrl));
            return;
        }
        disableAllButtons();
        RequestBuilder getLgsRequestBuilder = new RequestBuilder(RequestBuilder.GET, getLgsUrl);
        getLgsRequestBuilder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() != 200) {
                    showErrorAlert(stringMessages.importGetLeaderboardsFailed(response.getStatusCode(), getLgsUrl));
                }
                clearListBox();
                String body = response.getText();
                if (body == null || body.isEmpty()) {
                    showErrorAlert(stringMessages.importNoDataReturned());
                    changeButtonStateAccordingToApplicationState();
                    return;
                }
                JSONArray leaderboardGroups = JSONParser.parseStrict(body).isArray();
                List<String> toSort = new ArrayList<String>();
                for (int i = 0; i < leaderboardGroups.size(); i++) {
                    toSort.add(leaderboardGroups.get(i).isString().stringValue());
                }
                Collections.sort(toSort);
                allLeaderboardGroupNames = toSort;
                leaderboardgroupListBox.setVisibleItemCount(leaderboardGroups.size());
                for (String lgName : toSort) {
                    leaderboardgroupListBox.addItem(lgName);
                }
                changeButtonStateAccordingToApplicationState();
            }

            @Override
            public void onError(Request request, Throwable exception) {
                showErrorAlert(stringMessages.importServerError());
                changeButtonStateAccordingToApplicationState();
            }
        });
        try {
            getLgsRequestBuilder.send();
        } catch (RequestException e) {
            showErrorAlert(stringMessages.importServerError());
            changeButtonStateAccordingToApplicationState();
        }
    }

    private String createGetLgsUrl(String host) {
        StringBuffer urlBuffer = new StringBuffer(host);
        appendHttpAndSlashIfNeeded(host, urlBuffer);
        urlBuffer.append("sailingserver/api/v1/leaderboardgroups");
        return urlBuffer.toString();
    }

    private void appendHttpAndSlashIfNeeded(String host, StringBuffer urlBuffer) {
        if (!host.endsWith("/")) {
            urlBuffer.append("/");
        }
        if (!host.startsWith("http://")) {
            urlBuffer.insert(0, "http://");
        }
    }

    private void showErrorAlert(String string) {
        Window.alert(string);
    }

    public boolean isValidUrl(String url, boolean topLevelDomainRequired) {
        if (urlValidator == null || urlPlusTldValidator == null) {
            urlValidator = RegExp
                    .compile("^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");
            urlPlusTldValidator = RegExp
                    .compile("^((ftp|http|https)://[\\w@.\\-\\_]+\\.[a-zA-Z]{2,}(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");
        }
        return (topLevelDomainRequired ? urlPlusTldValidator : urlValidator).exec(url) != null;
    }

    private void addContentToLeftPanel(VerticalPanel contentPanel) {
        contentPanel.add(new Label(stringMessages.importLeaderboardGroups()));
        
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.add(new Label(stringMessages.filterName() + ":"));
        TextBox filterBox = new TextBox();
        setFilterHandler(filterBox);
        filterPanel.add(filterBox);
        contentPanel.add(filterPanel);

        leaderboardgroupListBox = new ListBox(true);
        addSelectionChangedListener();
        contentPanel.add(leaderboardgroupListBox);
        
        overrideSwitch = new CheckBox(stringMessages.importOverrideSwitchLabel());
        overrideSwitch.setValue(false);
        contentPanel.add(overrideSwitch);

        importLeaderboardGroupsButton = new Button(stringMessages.importSelectedLeaderboardGroups());
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
                clearListBox();
                int visibleNameCount = 0;
                List<String> filterTexts = Arrays.asList(filterBox.getText().split(" "));
                for(String name : allLeaderboardGroupNames) {
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
        });
    }

    public void clearListBox() {
        int itemCount = leaderboardgroupListBox.getItemCount();
        for (int i = itemCount - 1; i >= 0; i--) {
            leaderboardgroupListBox.removeItem(i);
        }
    }

}
