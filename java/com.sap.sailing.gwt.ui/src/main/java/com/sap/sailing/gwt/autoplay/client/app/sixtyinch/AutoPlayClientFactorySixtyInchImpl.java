package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactoryBase;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigatorImpl;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.StartClassicPlace;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerView;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.StartView;
import com.sap.sailing.gwt.autoplay.client.places.startsixtyinch.views.ConfigViewImpl;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactorySixtyInchImpl extends AutoPlayClientFactoryBase<PlaceNavigatorSixtyInch>
        implements AutoPlayClientFactorySixtyInch {

    private SixtyInchContext currentContext;
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();

    public AutoPlayClientFactorySixtyInchImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new PlaceNavigatorImpl(placeController));
    }

    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus, PlaceController placeController,
            PlaceNavigatorSixtyInch navigator) {
        super(new AutoPlayMainViewSixtyInchImpl(eventBus), eventBus, placeController, navigator);

    }
    
    @Override
    public StartView createStartView() {
        return new ConfigViewImpl(this);
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
        return new StartClassicPlace();
    }

    @Override
    public void setSlideContext(SixtyInchContext configurationSixtyInch) {
        this.currentContext = configurationSixtyInch;
    }

    @Override
    public SixtyInchContext getSlideCtx() {
        if (currentContext == null) {
            getEventBus().fireEvent(new AutoPlayFailureEvent("No autoplay context found"));
        }
        return currentContext;
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

}
