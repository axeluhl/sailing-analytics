package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class PlayerActivity extends AbstractActivity {
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
        
        UUID eventUUID = UUID.fromString(playerPlace.getConfiguration().getEventUidAsString());
        clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
                //RaceBoardPerspectiveSettings readRaceboardConfiguration = readRaceboardConfiguration();

                PlayerView view = clientFactory.createPlayerView();
                panel.setWidget(view.asWidget());
                RootLayoutPanel.get().add(view.asWidget());

                autoPlayController = new AutoPlayController(clientFactory.getSailingService(), clientFactory
                        .getMediaService(), clientFactory.getUserService(), clientFactory.getErrorReporter(), 
                        playerPlace.getConfiguration(), userAgent, delayToLiveMillis, showRaceDetails,
                        playerPlace.getRaceboardPerspectiveSettings().getA(), view, /** TODO: pass leaderboardSettings*/ null);
                autoPlayController.updatePlayMode(AutoPlayModes.Leaderboard);
            }

            @Override
            public void onFailure(Throwable caught) {
                createErrorView("Error while loading the event with service getEventById()", caught, panel);
            }
        }); 
    }
    
//    private RaceBoardPerspectiveSettings readRaceboardConfiguration() {
//        Boolean autoSelectMedia = true;
//
//        final boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(
//                RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_LEADERBOARD, true /* default */);
//        final boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(
//                RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_WINDCHART, false /* default */);
//        final boolean showViewStreamlets = GwtHttpRequestUtils.getBooleanParameter(
//                RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_STREAMLETS, false /* default */);
//        final boolean showViewStreamletColors = GwtHttpRequestUtils.getBooleanParameter(
//                RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_STREAMLET_COLORS, false /* default */);
//        final boolean showViewSimulation = GwtHttpRequestUtils.getBooleanParameter(
//                RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_SIMULATION, false /* default */);
//        final boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(
//                RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_COMPETITORSCHART, false /* default */);
//        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(RaceBoardPerspectiveSettings.PARAM_VIEW_COMPETITOR_FILTER, null /* default*/);
//        final String defaultMedia = GwtHttpRequestUtils.getStringParameter(RaceBoardPerspectiveSettings.PARAM_DEFAULT_MEDIA, null /* default */);
//        
//        return new RaceBoardPerspectiveSettings(activeCompetitorsFilterSetName, showLeaderboard,
//                showWindChart, showCompetitorsChart, showViewStreamlets, showViewStreamletColors, showViewSimulation, /* canReplayWhileLiveIsPossible */false, autoSelectMedia, defaultMedia);
//    }

    private void createErrorView(String errorMessage, Throwable errorReason, AcceptsOneWidget panel) {
        ErrorView view = clientFactory.createErrorView(errorMessage, errorReason);
        panel.setWidget(view.asWidget());
    }
}
