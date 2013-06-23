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
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLEncoder;

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

    public MasterDataImportPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;

        HorizontalPanel serverAddressPanel = new HorizontalPanel();
        serverAddressPanel.add(new Label("Remote host:"));
        hostBox = new TextBox();
        hostBox.setText("http://prod2.sapsailing.com/");
        hostBox.setWidth("300px");
        serverAddressPanel.add(hostBox);
        fetchIdsButton = new Button("Fetch Events and Leaderboard Groups");
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
        String getMasterDataUrl = createGetMasterDataForLgsUrl(currentHost);
        if (!isValidUrl(getMasterDataUrl, false)) {
            showErrorAlert("Not a valid URL for fetching leaderboardgroups masterdata: " + getMasterDataUrl);
            return;
        }
        String[] groupNames = createLeaderBoardGroupNamesFromListBox();
        String query = createLeaderboardQuery(groupNames);
        RequestBuilder getLgsMasterDataRequestBuilder = new RequestBuilder(RequestBuilder.GET, getMasterDataUrl
                + URLEncoder.encode(query));

        getLgsMasterDataRequestBuilder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() != 200) {
                    showErrorAlert("GET leaderboardgroups master data request failed with error code: "
                            + response.getStatusCode());
                    return;
                }
                fireLgsImportRequest(response);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                showErrorAlert("GET leaderboardgroups master data request failed with Server error: "
                        + exception.getLocalizedMessage());
            }
        });
        try {
            getLgsMasterDataRequestBuilder.send();
        } catch (RequestException e) {
            showErrorAlert("GET leaderboardgroups request failed: " + e.getLocalizedMessage());
        }
    }

    private String createLeaderboardQuery(String[] groupNames) {
        StringBuffer queryStringBuffer = new StringBuffer("?");
        for (int i = 0; i < groupNames.length; i++) {
            queryStringBuffer.append("names[]=" + groupNames[i] + "&");
        }
        if (queryStringBuffer.length() == 1) {
            return "";
        } else {
            // Delete last "&"
            queryStringBuffer.deleteCharAt(queryStringBuffer.length() - 1);
        }
        return queryStringBuffer.toString();
    }

    protected void fireLgsImportRequest(Response response) {
        RequestBuilder postLgsMasterDataRequestBuilder = new RequestBuilder(RequestBuilder.POST,
                createPostLgsMasterDataUrl());
        postLgsMasterDataRequestBuilder.setRequestData(response.getText());
        postLgsMasterDataRequestBuilder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                JSONValue creationCount = JSONParser.parseStrict(response.getText());
                JSONObject creationCountObj = (JSONObject) creationCount;
                int leaderboardsCreated = (int) ((JSONNumber) creationCountObj.get("leaderboards")).doubleValue();
                int leaderboardGroupsCreated = (int) ((JSONNumber) creationCountObj.get("leaderboardGroups"))
                        .doubleValue();
                int eventsCreated = (int) ((JSONNumber) creationCountObj.get("events"))
                        .doubleValue();
                int regattasCreated = (int) ((JSONNumber) creationCountObj.get("regattas"))
                        .doubleValue();
                showSuccessAlert(leaderboardsCreated, leaderboardGroupsCreated, eventsCreated, regattasCreated);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                showErrorAlert("POST leaderboardgroups master data request failed with Server error: "
                        + exception.getLocalizedMessage());
            }
        });
        try {
            postLgsMasterDataRequestBuilder.send();
        } catch (RequestException e) {
            showErrorAlert("POST leaderboardgroups master data request failed with Server error: "
                    + e.getLocalizedMessage());
        }
    }

    protected void showSuccessAlert(int leaderboardsCreated, int leaderboardGroupsCreated, int eventsCreated, int regattasCreated) {
        Window.alert(stringMessages.importSuccess(leaderboardGroupsCreated, leaderboardsCreated, eventsCreated, regattasCreated));

    }

    private String createPostLgsMasterDataUrl() {
        return "http://" + Window.Location.getHost() + "/sailingserver/masterdata/import/leaderboardgroups";
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

    private String createGetMasterDataForLgsUrl(String host) {
        StringBuffer urlBuffer = new StringBuffer(host);
        appendHttpAndSlashIfNeeded(host, urlBuffer);
        urlBuffer.append("sailingserver/masterdata/leaderboardgroups");
        return urlBuffer.toString();
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

        importLeaderboardGroupsButton = new Button("Import selected LGs");
        contentPanel.add(importLeaderboardGroupsButton);
    }

}
