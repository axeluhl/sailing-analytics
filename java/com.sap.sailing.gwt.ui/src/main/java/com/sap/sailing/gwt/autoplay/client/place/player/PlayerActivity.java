package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.place.start.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.place.start.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

public class PlayerActivity extends AbstractActivity {
    private final PlayerClientFactory clientFactory;
    private final PlayerPlace playerPlace;
    private AutoPlayController autoPlayController;

    public PlayerActivity(PlayerPlace playerPlace, PlayerClientFactory clientFactory) {
        this.playerPlace = playerPlace;
        this.clientFactory = clientFactory;
        this.autoPlayController = null;
    }

    private StrippedLeaderboardDTO getSelectedLeaderboard(EventDTO event, String leaderBoardName) {
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

        // get the event
        SettingsToStringSerializer stringSerializer = new SettingsToStringSerializer();
        AutoPlayerContext context = new AutoPlayerContext();
        stringSerializer.fromString(playerPlace.getContext(), context);
        final UUID eventUUID = context.getEventUidAsString();

        AsyncCallback<EventDTO> getEventByIdAsyncCallback = new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                StrippedLeaderboardDTO leaderBoardDTO = getSelectedLeaderboard(event, context.getLeaderboardName());
                AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderBoardDTO);
                PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings = stringSerializer
                        .fromString(playerPlace.getContext(), autoplayLifecycle.createDefaultSettings());

                clientFactory.getUserService().updateUser(true);

                UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
                PlayerView view = clientFactory.createPlayerView();
                panel.setWidget(view.asWidget());

                autoPlayController = new AutoPlayController(clientFactory.getSailingService(),
                        clientFactory.getMediaService(), clientFactory.getUserService(),
                        clientFactory.getErrorReporter(), context, autoplaySettings, userAgent, view,
                        autoplayLifecycle);

                autoPlayController.updatePlayMode(AutoPlayModes.Leaderboard);
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
