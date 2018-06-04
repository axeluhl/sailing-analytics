package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;

public abstract class AutoPlayPresenterConfigured<P extends Place> extends AutoPlayPresenterBase<P> {
    private EventBus eventBus;

    public AutoPlayPresenterConfigured(P place, AutoPlayClientFactory clientFactory) {
        super(place, clientFactory);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public final void start(AcceptsOneWidget panel, EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            if (getClientFactory().isConfigured()) {
                try {
                    startConfigured(panel);
                } catch (Exception e) {
                    getEventBus().fireEvent(new AutoPlayFailureEvent(e, "Start failed"));
                }
            } else {
                GWT.log("Not configured, go back to start place");
                getClientFactory().getPlaceController().goTo(getClientFactory().getDefaultPlace());
            }
        } catch (Exception e) {
            getEventBus().fireEvent(new AutoPlayFailureEvent(e, "Pre-Init failed"));
        }
    }

    public abstract void startConfigured(AcceptsOneWidget panel);
}
