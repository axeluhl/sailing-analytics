package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.place.player.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerView;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContext;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartViewSixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.place.start.StartView;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactorySixtyInchImpl extends AutoPlayClientFactoryBase<PlaceNavigatorSixtyInch>
        implements AutoPlayClientFactorySixtyInch {

    private SlideContext currentContext;
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
        return new StartViewSixtyInchImpl(this);
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
        return new StartPlaceSixtyInch();
    }

    @Override
    public void setSlideContext(SlideContext configurationSixtyInch) {
        this.currentContext = configurationSixtyInch;
    }

    @Override
    public SlideContext getSlideCtx() {
        if (currentContext == null) {
            getEventBus().fireEvent(new AutoplayFailureEvent("No autoplay context found"));
        }
        return currentContext;
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

}
