package com.sap.sailing.gwt.ui.client.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;
import com.sap.sse.common.search.KeywordQuery;

/**
 * Sample entry point demonstrating the search capabilites of
 * {@link SailingServiceAsync#search(String, com.sap.sse.common.search.KeywordQuery, AsyncCallback)} and
 * {@link SailingServiceAsync#getSearchServerNames(AsyncCallback)}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class SearchEntryPoint extends AbstractEntryPoint {
    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        VerticalPanel searchAndResults = new VerticalPanel();
        searchAndResults.setSize("100%", "30em");
        RootPanel.get().add(searchAndResults);
        HorizontalPanel searchPanel = new HorizontalPanel();
        searchAndResults.add(searchPanel);
        searchPanel.add(new Label("Search"));
        final TextBox searchBox = new TextBox();
        searchPanel.add(searchBox);
        Button searchButton = new Button("Search");
        searchPanel.add(searchButton);
        final TextArea resultsLabel = new TextArea();
        resultsLabel.setSize("90%", "70%");
        resultsLabel.setEnabled(false);
        searchAndResults.add(resultsLabel);
        searchButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final KeywordQuery query = new KeywordQuery(searchBox.getText().split(" "));
                resultsLabel.setText("");
                sailingService.getSearchServerNames(new AsyncCallback<Iterable<String>>() {
                    @Override
                    public void onSuccess(Iterable<String> serverNames) {
                        search(resultsLabel, query, /* null meaning the "local" server */ null);
                        for (String serverName : serverNames) {
                            search(resultsLabel, query, serverName);
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error: "+caught.getMessage());
                    }
                });
            }
        });
    }

    private void search(final TextArea resultsLabel, final KeywordQuery query, final String serverName) {
        sailingService.search(serverName, query, new AsyncCallback<Iterable<LeaderboardSearchResultDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error: "+caught.getMessage());
            }

            @Override
            public void onSuccess(Iterable<LeaderboardSearchResultDTO> result) {
                for (LeaderboardSearchResultDTO r : result) {
                    resultsLabel.setText(resultsLabel.getText()+"\n"+r);
                }
            }
        });
    }
}
