package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.ClassicConfigPlace;

public abstract class ClassicConfiguredPresenter<P extends Place> extends ClassicPresenterBase<P> {
    private EventBus eventBus;

    public ClassicConfiguredPresenter(P place, AutoPlayClientFactoryClassic clientFactory) {
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
                            getEventBus().fireEvent(new AutoPlayFailureEvent(e, "Start failed"));
                        }
                    }
                });
            } else {
                GWT.log("Not configured, go back to start place");
                getClientFactory().getPlaceController().goTo(new ClassicConfigPlace());
            }
        } catch (Exception e) {
            getEventBus().fireEvent(new AutoPlayFailureEvent(e, "Pre-Init failed"));
        }
    }

    protected void init(AcceptsOneWidget panel, Command whenReady) {
        whenReady.execute();
    }

    public abstract void startConfigured(AcceptsOneWidget panel);
}
