package com.sap.sailing.gwt.ui.tv;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewConfiguration;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class TVEntryPoint extends AbstractSailingEntryPoint {
    
    private static final String PARAM_LEADERBOARD_NAME = "name";
    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    private static final String PARAM_EMBEDDED = "embedded";
    private static final String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    private static final String PARAM_DELAY_TO_LIVE_MILLIS = "delayToLiveMillis";
    private String leaderboardName;
    private String leaderboardGroupName;
    private TVViewController tvViewController;
    private RaceBoardViewConfiguration raceboardViewConfig;
    
    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);

    @Override
    public void doOnModuleLoad() {     
        super.doOnModuleLoad();
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);

        final boolean showRaceDetails = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_RACE_DETAILS, true); 
        final boolean embedded = GwtHttpRequestUtils.getBooleanParameter(PARAM_EMBEDDED, false); 
        final long delayToLiveMillis = Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS) != null ? Long
                .valueOf(Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS)) : 5000l; // default 5s
        final boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_LEADERBOARD, true /* default */);
        final boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_WINDCHART, false /* default */);
        final boolean showViewStreamlets = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_STREAMLETS, false /* default */);
        final boolean showViewSimulation = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_SIMULATION, false /* default */);
        final boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_COMPETITORSCHART, false /* default */);
        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_VIEW_COMPETITOR_FILTER, null /* default*/);
        final String defaultMedia = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_DEFAULT_MEDIA, null /* default */);
        final boolean autoSelectMedia = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_AUTOSELECT_MEDIA, false);
        raceboardViewConfig = new RaceBoardViewConfiguration(activeCompetitorsFilterSetName, showLeaderboard,
                showWindChart, showCompetitorsChart, showViewStreamlets, showViewSimulation, /* canReplayWhileLiveIsPossible */false, autoSelectMedia, defaultMedia);

        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                leaderboardName = Window.Location.getParameter(PARAM_LEADERBOARD_NAME);
                leaderboardGroupName = Window.Location.getParameter(PARAM_LEADERBOARD_GROUP_NAME);
                if (leaderboardNames.contains(leaderboardName)) {
                    createUI(showRaceDetails, embedded, delayToLiveMillis);
                } else {
                    RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: " + t.getMessage());
            }
        });
    }
    
    private void createUI(boolean showRaceDetails, boolean embedded, long delayToLiveMillis) {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        LogoAndTitlePanel logoAndTitlePanel = null;
        if (!embedded) {
            String leaderboardDisplayName = Window.Location.getParameter("displayName");
            if(leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
                leaderboardDisplayName = leaderboardName;
            }
            logoAndTitlePanel = new LogoAndTitlePanel(leaderboardGroupName, leaderboardDisplayName, getStringMessages(), this, getUserService());
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            mainPanel.addNorth(logoAndTitlePanel, 68);
        }
        
        tvViewController = new TVViewController(sailingService, mediaService, getUserService(), getStringMessages(),
                this, leaderboardGroupName, leaderboardName, userAgent, logoAndTitlePanel, mainPanel,
                delayToLiveMillis, showRaceDetails, raceboardViewConfig);
        tvViewController.updateTvView(TVViews.Leaderboard);
    }
}
