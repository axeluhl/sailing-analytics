package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.StartPlaceSixtyInch;

public abstract class ConfiguredPresenter<P extends Place> extends PresenterBase<P> {
    private EventBus eventBus;

    public ConfiguredPresenter(P place, AutoPlayClientFactorySixtyInch clientFactory) {
        super(place, clientFactory);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public final void start(AcceptsOneWidget panel, EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            if (getClientFactory().getSlideCtx() != null && getClientFactory().getSlideCtx().getEvent() != null) {
                init(panel, new Command() {
                    private boolean hasStarted = false;
                    @Override
                    public void execute() {
                        if (hasStarted)
                            return;
                        hasStarted = true;
                        try {
                            startConfigured(panel);
                        } catch (Exception e) {
                            hasStarted = false;
                            getEventBus().fireEvent(new AutoplayFailureEvent(e, "Start failed"));
                        }
                    }
                });
            } else {
                GWT.log("Not configured, go back to start place");
                getClientFactory().getPlaceController().goTo(new StartPlaceSixtyInch());
            }
        } catch (Exception e) {
            getEventBus().fireEvent(new AutoplayFailureEvent(e, "Pre-Init failed"));
        }
    }

    protected void init(AcceptsOneWidget panel, Command whenReady) {
        whenReady.execute();
    }

    public abstract void startConfigured(AcceptsOneWidget panel);
}
