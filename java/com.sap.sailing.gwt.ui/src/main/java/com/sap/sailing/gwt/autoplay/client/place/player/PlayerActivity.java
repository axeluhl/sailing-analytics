package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class PlayerActivity extends AbstractActivity {
    private final PlayerClientFactory clientFactory;
    private final PlayerPlace playerPlace;

    public PlayerActivity(PlayerPlace playerPlace, PlayerClientFactory clientFactory) {
        this.playerPlace = playerPlace;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        UUID eventUUID = UUID.fromString(playerPlace.getEventUuidAsString());
        clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                PlayerView view = clientFactory.createPlayerView(event, playerPlace.getLeaderboardIdAsNameString());
                panel.setWidget(view.asWidget());
            }

            @Override
            public void onFailure(Throwable caught) {
//                createErrorView("Error while loading the event with service getEventById()", caught, panel);
            }
        }); 
    }
}
