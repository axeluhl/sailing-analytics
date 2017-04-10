package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

/**
 * Trigger "in between/ idle mode" path in autoplay
 */
public class LiferaceDetectedEvent extends GwtEvent<LiferaceDetectedEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private RegattaAndRaceIdentifier lifeRace;
    private GetMiniLeaderboardDTO leaderboard;

    public LiferaceDetectedEvent(RegattaAndRaceIdentifier lifeRace, GetMiniLeaderboardDTO leaderboard) {
        this.lifeRace = lifeRace;
        this.leaderboard = leaderboard;
    }

    public RegattaAndRaceIdentifier getLifeRace() {
        return lifeRace;
    }

    public GetMiniLeaderboardDTO getLeaderboard() {
        return leaderboard;
    }


    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void onLiferaceDetected(LiferaceDetectedEvent e);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onLiferaceDetected(this);
    }
}
