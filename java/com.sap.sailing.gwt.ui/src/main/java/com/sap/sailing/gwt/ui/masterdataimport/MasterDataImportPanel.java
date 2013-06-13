package com.sap.sailing.gwt.ui.masterdataimport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
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

public class MasterDataImportPanel extends VerticalPanel {

    private ListBox eventListBox;
    private ListBox leaderboardgroupListBox;
    private Button importEventsButton;
    private TextBox hostBox;
    private Button importLeaderboardGroupsButton;
    private Button fetchIdsButton;

    private RegExp urlValidator;
    private RegExp urlPlusTldValidator;

    /*
     * TODO: use string messages
     */
    private final StringMessages stringMessages;

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

        HorizontalPanel splitPanel = new HorizontalPanel();
        ScrollPanel leftScrollPanel = new ScrollPanel();
        ScrollPanel rightScrollPanel = new ScrollPanel();
        splitPanel.add(leftScrollPanel);
        splitPanel.add(rightScrollPanel);
        this.add(splitPanel);

        VerticalPanel leftContentPanel = new VerticalPanel();
        leftScrollPanel.setWidget(leftContentPanel);
        VerticalPanel rightContentPanel = new VerticalPanel();
        rightScrollPanel.setWidget(rightContentPanel);

        addContentToLeftPanel(leftContentPanel);
        addContentToRightPanel(rightContentPanel);

        setListeners();
    }

    private void setListeners() {
        fetchIdsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                fireIdRequestsAndFillLists();
            }
        });
    }

    protected void fireIdRequestsAndFillLists() {
        String host = hostBox.getText();
        if (host != null && !host.isEmpty()) {
            fireEventIdRequestAndFillList(host);
            fireLgIdRequestAndFillList(host);
        }
    }

    private void fireLgIdRequestAndFillList(String host) {
        String getLgsUrl = createGetLgsUrl(host);
        if (!isValidUrl(getLgsUrl, false)) {
            showErrorAlert("Not a valid URL for fetching leaderboardgroups: " + getLgsUrl);
            return;
        }
        RequestBuilder getLgsRequestBuilder = new RequestBuilder(RequestBuilder.GET, getLgsUrl);
        getLgsRequestBuilder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() != 200) {
                    showErrorAlert("GET leaderboardgroups request failed with error code: " + response.getStatusCode());
                }
                int itemCount = leaderboardgroupListBox.getItemCount();
                for (int i = itemCount - 1; i >= 0; i--) {
                    leaderboardgroupListBox.removeItem(i);
                }
                JSONArray leaderboardGroups = JSONParser.parseStrict(response.getText()).isArray();
                for (int i = 0; i < leaderboardGroups.size(); i++) {
                    JSONString leaderboardGroupName = leaderboardGroups.get(i).isString();
                    leaderboardgroupListBox.addItem(leaderboardGroupName.stringValue());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                showErrorAlert("GET leaderboardgroups request failed with Server error: " + exception.getLocalizedMessage());
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

    private void fireEventIdRequestAndFillList(String host) {
        String getEventsUrl = createGetEventsUrl(host);
        if (!isValidUrl(getEventsUrl, false)) {
            showErrorAlert("Not a valid URL for fetching events: " + getEventsUrl);
            return;
        }
        RequestBuilder getEventsRequestBuilder = new RequestBuilder(RequestBuilder.GET, getEventsUrl);
        getEventsRequestBuilder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() != 200) {
                    showErrorAlert("GET events request failed with error code: " + response.getStatusCode());
                }
                int itemCount = eventListBox.getItemCount();
                for (int i = itemCount - 1; i >= 0; i--) {
                    eventListBox.removeItem(i);
                }
                JSONArray events = JSONParser.parseStrict(response.getText()).isArray();
                for (int i = 0; i < events.size(); i++) {
                    JSONObject event = events.get(i).isObject();
                    eventListBox.addItem(event.get("name").isString().stringValue(), event.get("id").isString()
                            .stringValue());
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                showErrorAlert("GET events request failed with Server error: " + exception.getLocalizedMessage());
            }
        });
        try {
            getEventsRequestBuilder.send();
        } catch (RequestException e) {
            showErrorAlert("GET events request failed: " + e.getLocalizedMessage());
        }
    }

    private void showErrorAlert(String string) {
        Window.alert(string);
    }

    private String createGetEventsUrl(String host) {
        StringBuffer urlBuffer = new StringBuffer(host);
        appendHttpAndSlashIfNeeded(host, urlBuffer);
        urlBuffer.append("sailingserver/events");
        return urlBuffer.toString();
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

    private void addContentToRightPanel(VerticalPanel contentPanel) {
        contentPanel.add(new Label("Events:"));

        eventListBox = new ListBox();
        contentPanel.add(eventListBox);

        importEventsButton = new Button("Import selected events");
        contentPanel.add(importEventsButton);
    }

    private void addContentToLeftPanel(VerticalPanel contentPanel) {
        contentPanel.add(new Label("Leaderboard Groups:"));

        leaderboardgroupListBox = new ListBox();
        contentPanel.add(leaderboardgroupListBox);

        importLeaderboardGroupsButton = new Button("Import selected LGs");
        contentPanel.add(importLeaderboardGroupsButton);
    }

}
