package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewConfiguration;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class PlayerActivity extends AbstractActivity implements ErrorReporter {
    private final PlayerClientFactory clientFactory;
    private final PlayerPlace playerPlace;
    private AutoPlayController autoPlayController; 

    private static final String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    private static final String PARAM_DELAY_TO_LIVE_MILLIS = "delayToLiveMillis";

    public PlayerActivity(PlayerPlace playerPlace, PlayerClientFactory clientFactory) {
        this.playerPlace = playerPlace;
        this.clientFactory = clientFactory;
        this.autoPlayController = null;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final boolean showRaceDetails = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_RACE_DETAILS, false); 
        final long delayToLiveMillis = Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS) != null ? Long
                .valueOf(Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS)) : 5000l; // default 5s
        
        UUID eventUUID = UUID.fromString(playerPlace.getEventUuidAsString());
        clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
                RaceBoardViewConfiguration readRaceboardConfiguration = readRaceboardConfiguration();

                PlayerView view = clientFactory.createPlayerView();
                panel.setWidget(view.asWidget());

                autoPlayController = new AutoPlayController(clientFactory.getSailingService(), clientFactory.getMediaService(),
                        PlayerActivity.this, /*leaderboardGroupName*/ "" , playerPlace.getLeaderboardIdAsNameString(), playerPlace.getLeaderboardZoomAsString(),
                        userAgent, delayToLiveMillis, showRaceDetails, readRaceboardConfiguration, view);
                autoPlayController.updatePlayMode(AutoPlayModes.Leaderboard);
            }

            @Override
            public void onFailure(Throwable caught) {
                createErrorView("Error while loading the event with service getEventById()", caught, panel);
            }
        }); 
    }
    
    private RaceBoardViewConfiguration readRaceboardConfiguration() {
        final boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_LEADERBOARD, true /* default */);
        final boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_WINDCHART, false /* default */);
        final boolean showViewStreamlets = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_STREAMLETS, false /* default */);
        final boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(
                RaceBoardViewConfiguration.PARAM_VIEW_SHOW_COMPETITORSCHART, false /* default */);
        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_VIEW_COMPETITOR_FILTER, null /* default*/);
        final String defaultMedia = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_DEFAULT_MEDIA, null /* default */);
        final boolean autoSelectMedia = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_AUTOSELECT_MEDIA, false);
        
        return new RaceBoardViewConfiguration(activeCompetitorsFilterSetName, showLeaderboard,
                showWindChart, showCompetitorsChart, showViewStreamlets, /* canReplayWhileLiveIsPossible */false, autoSelectMedia, defaultMedia);
    }

    private void createErrorView(String errorMessage, Throwable errorReason, AcceptsOneWidget panel) {
        ErrorView view = clientFactory.createErrorView(errorMessage, errorReason);
        panel.setWidget(view.asWidget());
    }
    
    @Override
    public void reportError(String message) {
        Window.alert(message);
    }

    @Override
    public void reportError(String message, boolean silentMode) {
        if (silentMode) {
            Window.setStatus(message);
        } else {
            reportError(message);
        }
    }

    @Override
    public void reportPersistentInformation(String message) {
        Window.setStatus(message);
    }

}
