package com.sap.sailing.gwt.ui.masterdataimport;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MasterDataImportPanel extends VerticalPanel {

    private ListBox leaderboardgroupListBox;
    private TextBox hostBox;
    private Button importLeaderboardGroupsButton;
    private Button fetchIdsButton;

    private RegExp urlValidator;
    private RegExp urlPlusTldValidator;

    /*
     * TODO: use string messages
     */
    private final StringMessages stringMessages;
    private String currentHost;
    private SailingServiceAsync sailingService;
    private CheckBox overrideSwitch;

    public MasterDataImportPanel(StringMessages stringMessages, SailingServiceAsync sailingService) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;

        HorizontalPanel serverAddressPanel = new HorizontalPanel();
        serverAddressPanel.add(new Label("Remote host:"));
        hostBox = new TextBox();
        hostBox.setText("http://prod2.sapsailing.com/");
        hostBox.setWidth("300px");
        serverAddressPanel.add(hostBox);
        fetchIdsButton = new Button("Fetch Leaderboard Group List");
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
        boolean override = overrideSwitch.getValue();
        sailingService.importMasterData(currentHost, groupNames, override,
                new AsyncCallback<MasterDataImportObjectCreationCount>() {

                    @Override
                    public void onSuccess(MasterDataImportObjectCreationCount result) {
                        int leaderboardsCreated = result.getLeaderboardCount();
                        int leaderboardGroupsCreated = result.getLeaderboardGroupCount();
                        int eventsCreated = result.getEventCount();
                        int regattasCreated = result.getRegattaCount();
                        showSuccessAlert(leaderboardsCreated, leaderboardGroupsCreated, eventsCreated, regattasCreated);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        showErrorAlert(caught.getLocalizedMessage());
                    }
                });
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
            showErrorAlert("Not a valid URL for fetching leaderboardgroups: " + getLgsUrl);
            return;
        }
        RequestBuilder getLgsRequestBuilder = new RequestBuilder(RequestBuilder.GET, getLgsUrl);
        getLgsRequestBuilder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() != 200) {
                    showErrorAlert("GET leaderboardgroups request failed with error code: " + response.getStatusCode()
                            + " For url: " + getLgsUrl);
                }
                int itemCount = leaderboardgroupListBox.getItemCount();
                for (int i = itemCount - 1; i >= 0; i--) {
                    leaderboardgroupListBox.removeItem(i);
                }
                String body = response.getText();
                if (body == null || body.isEmpty()) {
                    showErrorAlert("No data was returned by remote server.");
                    return;
                }
                JSONArray leaderboardGroups = JSONParser.parseStrict(body).isArray();
                leaderboardgroupListBox.setVisibleItemCount(leaderboardGroups.size());
                for (int i = 0; i < leaderboardGroups.size(); i++) {
                    JSONString leaderboardGroupName = leaderboardGroups.get(i).isString();
                    leaderboardgroupListBox.addItem(leaderboardGroupName.stringValue());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                showErrorAlert("GET leaderboardgroups request failed with Server error: "
                        + exception.getLocalizedMessage());
            }
        });
        try {
            getLgsRequestBuilder.send();
        } catch (RequestException e) {
            showErrorAlert("GET leaderboardgroups request failed: " + e.getLocalizedMessage());
        }
    }

    private String createGetLgsUrl(String host) {
        StringBuffer urlBuffer = new StringBuffer(host);
        appendHttpAndSlashIfNeeded(host, urlBuffer);
        urlBuffer.append("sailingserver/leaderboardgroups");
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
        contentPanel.add(new Label("Leaderboard Groups:"));

        leaderboardgroupListBox = new ListBox(true);
        contentPanel.add(leaderboardgroupListBox);
        
        overrideSwitch = new CheckBox("Override existing data if names and ids match");
        overrideSwitch.setValue(false);
        contentPanel.add(overrideSwitch);

        importLeaderboardGroupsButton = new Button("Import selected Leaderboard Groups");
        contentPanel.add(importLeaderboardGroupsButton);
    }

}
