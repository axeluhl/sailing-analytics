package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactoryBase;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigatorImpl;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerView;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.config.ClassicConfigPlace;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactoryClassicImpl extends AutoPlayClientFactoryBase<PlaceNavigator>
        implements AutoPlayClientFactoryClassic {
    public AutoPlayClientFactoryClassicImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactoryClassicImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactoryClassicImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new PlaceNavigatorImpl(placeController));
    }

    private AutoPlayClientFactoryClassicImpl(EventBus eventBus, PlaceController placeController, PlaceNavigator navigator) {
        super(new AutoPlayMainViewDesktopImpl(), eventBus, placeController, navigator);
    }
    

    @Override
    public PlayerView createPlayerView() {
        return new DesktopPlayerView(getPlaceNavigator());
    }

    @Override
    public ErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return null;
    }

    @Override
    public Place getDefaultPlace() {
        return new ClassicConfigPlace();
    }


}
