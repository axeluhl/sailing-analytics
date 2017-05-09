package com.sap.sailing.gwt.autoplay.client.app.classic;

import java.util.UUID;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactoryBase;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContext;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContextImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayMainViewImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayNavigatorImpl;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.RootNodeClassic;
import com.sap.sailing.gwt.autoplay.client.places.config.classic.ClassicConfigPlace;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;


public class AutoPlayClientFactoryClassicImpl extends AutoPlayClientFactoryBase {

    private AutoPlayContext currentContext;
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();

    public AutoPlayClientFactoryClassicImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactoryClassicImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactoryClassicImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new AutoplayNavigatorImpl(placeController));
    }

    private AutoPlayClientFactoryClassicImpl(EventBus eventBus, PlaceController placeController,
            AutoPlayPlaceNavigator navigator) {
        super(new AutoPlayMainViewImpl(eventBus), eventBus, placeController, navigator);
    }
    

    @Override
    public ErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return null;
    }

    @Override
    public Place getDefaultPlace() {
        return new ClassicConfigPlace();
    }

    @Override
    public void setSlideContext(AutoPlayContext ctx) {
        this.currentContext = ctx;
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
        ClassicSetting context = new ClassicSetting();
        SettingsToStringSerializer stringSerializer = new SettingsToStringSerializer();
        stringSerializer.fromString(serializedSettings, context);
        final UUID eventUUID = context.getEventId();
        AsyncCallback<EventDTO> getEventByIdAsyncCallback = new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                StrippedLeaderboardDTO leaderBoardDTO = AutoplayHelper.getSelectedLeaderboard(event,
                        context.getLeaderboardName());
                AutoplayPerspectiveLifecycle autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderBoardDTO);
                PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings = stringSerializer
                        .fromString(serializedSettings, autoplayLifecycle.createDefaultSettings());
                setSlideContext(
                        new AutoPlayContextImpl(getEventBus(), autoplayLifecycle, autoplaySettings, context));
                // start sixty inch slide loop nodes...
                RootNodeClassic root = new RootNodeClassic(AutoPlayClientFactoryClassicImpl.this);
                root.start(getEventBus());
            }

            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();
            }
        };
        getSailingService().getEventById(eventUUID, true, getEventByIdAsyncCallback);
    }
}
