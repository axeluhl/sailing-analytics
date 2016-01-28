package com.sap.sse.security.ui.authentication;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

// TODO Refactor to com.sap.sse.gwt if needed
public class WrappedPlaceManagementController {
    
    public interface PlaceManagementConfiguration extends ActivityMapper {
        Place getStartPlace();
        AcceptsOneWidget getDisplay();
        void setPlaceController(PlaceController placeController);
    }

    private final PlaceManagementConfiguration wrappedActivityMapper;
    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController wrappedPlaceController = new PlaceController(eventBus);
    
    public WrappedPlaceManagementController(PlaceManagementConfiguration configuration) {
        this.wrappedActivityMapper = configuration;
        this.wrappedActivityMapper.setPlaceController(this.wrappedPlaceController);
        ActivityManager wrappedActivityManager = new ActivityManager(this.wrappedActivityMapper, eventBus);
        wrappedActivityManager.setDisplay(configuration.getDisplay());
    }

    public void start() {
        this.wrappedPlaceController.goTo(this.wrappedActivityMapper.getStartPlace());
    }
    
    public void goTo(Place newPlace) {
        this.wrappedPlaceController.goTo(newPlace);
    }
    
    public <H> HandlerRegistration addHandler(Type<H> type, H handler) {
        return eventBus.addHandler(type, handler);
    }

    public void fireEvent(AuthenticationContextEvent event) {
        eventBus.fireEvent(event);
    }
    
}
