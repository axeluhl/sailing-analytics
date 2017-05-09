package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactoryBase;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContext;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContextImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayMainViewImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayNavigatorImpl;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.RootNodeSixtyInch;
import com.sap.sailing.gwt.autoplay.client.places.config.sixtyinch.SixtyInchConfigPlace;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;


public class AutoPlayClientFactorySixtyInchImpl extends AutoPlayClientFactoryBase {

    private AutoPlayContext currentContext;
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();

    public AutoPlayClientFactorySixtyInchImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new AutoplayNavigatorImpl(placeController));
    }

    private AutoPlayClientFactorySixtyInchImpl(EventBus eventBus, PlaceController placeController,
            AutoPlayPlaceNavigator navigator) {
        super(new AutoPlayMainViewImpl(eventBus), eventBus, placeController, navigator);

    }


    @Override
    public ErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return null;
    }

    @Override
    public Place getDefaultPlace() {
        return new SixtyInchConfigPlace();
    }

    @Override
    public void setSlideContext(AutoPlayContext configurationSixtyInch) {
        this.currentContext = configurationSixtyInch;
    }

    @Override
    public AutoPlayContext getSlideCtx() {
        if (currentContext == null) {
            getEventBus().fireEvent(new AutoPlayFailureEvent("No autoplay context found"));
        }
        return currentContext;
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public void startRootNode(String serializedSettings) {
        SixtyInchSetting settings = new SixtyInchSetting();
        new SettingsToStringSerializer().fromString(serializedSettings, settings);
        setSlideContext(new AutoPlayContextImpl(getEventBus(), settings));
        // start sixty inch slide loop nodes...
        RootNodeSixtyInch root = new RootNodeSixtyInch(this);
        root.start(getEventBus());
    }
}
