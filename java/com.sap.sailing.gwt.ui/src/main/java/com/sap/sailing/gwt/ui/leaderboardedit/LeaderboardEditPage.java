package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

public class LeaderboardEditPage extends AbstractSailingEntryPoint {
    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);

        sailingService.getLeaderboardNames(new MarkedAsyncCallback<List<String>>(
                new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                String leaderboardName = Window.Location.getParameter("name");
                if (leaderboardNames.contains(leaderboardName)) {
                    LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages().editScores(), getStringMessages(), LeaderboardEditPage.this, getUserService());
                    logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
                    EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(sailingService, new AsyncActionsExecutor(), leaderboardName, null,
                            LeaderboardEditPage.this, getStringMessages(), userAgent);
                    leaderboardPanel.ensureDebugId("EditableLeaderboardPanel");
                    RootPanel.get().add(logoAndTitlePanel);
                    RootPanel.get().add(leaderboardPanel);
                } else {
                    RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
                }
            }
            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: "+t.getMessage());
            }
        }));
    }

}
