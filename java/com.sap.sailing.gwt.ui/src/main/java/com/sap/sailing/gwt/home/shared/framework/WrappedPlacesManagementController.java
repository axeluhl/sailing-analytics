package com.sap.sailing.gwt.home.shared.framework;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class WrappedPlacesManagementController {
    
    public interface StartPlaceActivityMapper extends ActivityMapper {
        Place getStartPlace();
    }

    private final StartPlaceActivityMapper wrappedActivityMapper;
    private final PlaceController wrappedPlaceController;
    
    public WrappedPlacesManagementController(StartPlaceActivityMapper wrappedActivityMapper, AcceptsOneWidget wrappedDisplay) {
        EventBus eventBus = new SimpleEventBus();
        this.wrappedActivityMapper = wrappedActivityMapper;
        this.wrappedPlaceController = new PlaceController(eventBus);
        ActivityManager wrappedActivityManager = new ActivityManager(this.wrappedActivityMapper, eventBus);
        wrappedActivityManager.setDisplay(wrappedDisplay);
    }

    public void start() {
        this.wrappedPlaceController.goTo(this.wrappedActivityMapper.getStartPlace());
    }
    
    public void goTo(Place newPlace) {
        this.wrappedPlaceController.goTo(newPlace);
    }
    
}
