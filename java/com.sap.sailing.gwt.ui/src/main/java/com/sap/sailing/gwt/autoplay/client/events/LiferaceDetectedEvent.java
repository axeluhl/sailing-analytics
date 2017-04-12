package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

/**
 * Trigger "in between/ idle mode" path in autoplay
 */
public class LiferaceDetectedEvent extends GwtEvent<LiferaceDetectedEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private RegattaAndRaceIdentifier lifeRace;

    public LiferaceDetectedEvent(RegattaAndRaceIdentifier lifeRace) {
        this.lifeRace = lifeRace;
    }

    public RegattaAndRaceIdentifier getLifeRace() {
        return lifeRace;
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
