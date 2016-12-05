package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderComponentContext;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardComponentContext;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.IOnDefaultSettingsLoaded;
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

    private StrippedLeaderboardDTO getSelectedLeaderboard(EventDTO event,String leaderBoardName) {
        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard.name.equals(leaderBoardName)) {
                    return leaderboard;
                }
            }
        }
        return null;
    }
    
    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final boolean showRaceDetails = GwtHttpRequestUtils.getBooleanParameter(PARAM_SHOW_RACE_DETAILS, false); 
        final long delayToLiveMillis = Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS) != null ? Long
                .valueOf(Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS)) : 5000l; // default 5s
        
        final UUID eventUUID = UUID.fromString(playerPlace.getConfiguration().getEventUidAsString());
        
        AsyncCallback<EventDTO> getEventByIdAsyncCallback = new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {

                //This place fixes a specific null pointer bug3950, once the settings are redone, this should be removed, and instead be done via playerplace (see comment there)
                StrippedLeaderboardDTO leaderBoardDTO = getSelectedLeaderboard(event,playerPlace.getConfiguration().getLeaderboardName());
                LeaderboardWithHeaderPerspectiveLifecycle leaderboardPerspectiveLifecycle = new LeaderboardWithHeaderPerspectiveLifecycle(leaderBoardDTO, StringMessages.INSTANCE);
                RaceBoardPerspectiveLifecycle raceboardPerspectiveLifecycle = new RaceBoardPerspectiveLifecycle(leaderBoardDTO, StringMessages.INSTANCE);
         
                final RaceBoardComponentContext raceBoardContext = new RaceBoardComponentContext(clientFactory.getUserService(), "AutoPlay.RaceBoard", raceboardPerspectiveLifecycle, null, null, playerPlace.getConfiguration().getLeaderboardName(), null, eventUUID);
                final LeaderboardWithHeaderComponentContext leaderboardWithHeaderContext = new LeaderboardWithHeaderComponentContext(clientFactory.getUserService(), "AutoPlay.Leaderboard", leaderboardPerspectiveLifecycle);
                

                
                AbstractComponentContextWithSettingsStorage.initMultipleDefaultSettings(raceBoardContext, leaderboardWithHeaderContext, new IOnDefaultSettingsLoaded() {
                    
                    @Override
                    public void onLoaded() {
                        UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
                        PlayerView view = clientFactory.createPlayerView();
                        panel.setWidget(view.asWidget());
                        
                        autoPlayController = new AutoPlayController(leaderboardWithHeaderContext, raceBoardContext, clientFactory.getSailingService(), clientFactory
                                .getMediaService(), clientFactory.getUserService(), clientFactory.getErrorReporter(), 
                                playerPlace.getConfiguration(), userAgent, delayToLiveMillis, showRaceDetails, view);
                        autoPlayController.updatePlayMode(AutoPlayModes.Leaderboard);
                    }
                });
                clientFactory.getUserService().updateUser(true);
            }

            @Override
            public void onFailure(Throwable caught) {
                createErrorView("Error while loading the event with service getEventById()", caught, panel);
            }
        };
        clientFactory.getSailingService().getEventById(eventUUID, true, getEventByIdAsyncCallback); 
    }

    private void createErrorView(String errorMessage, Throwable errorReason, AcceptsOneWidget panel) {
        ErrorView view = clientFactory.createErrorView(errorMessage, errorReason);
        panel.setWidget(view.asWidget());
    }
}
