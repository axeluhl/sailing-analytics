package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.player.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerView;
import com.sap.sailing.gwt.autoplay.client.place.start.DesktopStartView;
import com.sap.sailing.gwt.autoplay.client.place.start.StartView;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class DesktopApplicationClientFactory extends AbstractApplicationClientFactory implements AutoPlayAppClientFactory {
    public DesktopApplicationClientFactory() {
        this(new SimpleEventBus());
    }
    
    private DesktopApplicationClientFactory(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private DesktopApplicationClientFactory(EventBus eventBus, PlaceController placeController) {
        super(new DesktopApplicationView(new PlaceNavigatorImpl(placeController)), eventBus, placeController);
    }

    @Override
    public StartView createStartView() {
        return new DesktopStartView(getPlaceNavigator(), getEventBus());
    }

    @Override
    public PlayerView createPlayerView() {
        return new DesktopPlayerView(getPlaceNavigator());
    }

    @Override
    public ErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return null;
    }
}
