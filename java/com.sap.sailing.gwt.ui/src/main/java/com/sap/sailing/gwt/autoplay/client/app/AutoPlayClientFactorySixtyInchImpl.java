package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.player.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerView;
import com.sap.sailing.gwt.autoplay.client.place.start.DesktopStartView;
import com.sap.sailing.gwt.autoplay.client.place.start.StartView;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactorySixtyInchImpl extends AutoPlayClientFactoryBase implements AutoPlayClientFactory {
    public AutoPlayClientFactorySixtyInchImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new PlaceNavigatorImpl(placeController));
    }

    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus, PlaceController placeController, PlaceNavigator navigator) {
        super(new AutoPlayMainViewSixtyInchImpl(), eventBus, placeController, navigator);
    }
    
    @Override
    public StartView createStartView() {
        return new DesktopStartView(getPlaceNavigator(), getEventBus(), getUserService());
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
