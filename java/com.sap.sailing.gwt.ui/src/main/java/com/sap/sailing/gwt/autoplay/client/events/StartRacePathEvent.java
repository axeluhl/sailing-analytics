package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

/**
 * Trigger "in between/ idle mode" path in autoplay
 */
public class StartRacePathEvent extends GwtEvent<StartRacePathEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private GetMiniLeaderboardDTO dto;

    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void onFallbackToIdle(StartRacePathEvent e);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onFallbackToIdle(this);
    }
}
