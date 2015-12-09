package com.sap.sailing.gwt.home.shared.framework;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;

public class WrappedPlacesManagementController {
    
    public interface StartPlaceActivityMapper extends ActivityMapper {
        Place getStartPlace();
        void setPlaceController(PlaceController placeController);
    }

    private final StartPlaceActivityMapper wrappedActivityMapper;
    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController wrappedPlaceController = new PlaceController(eventBus);
    
    public WrappedPlacesManagementController(StartPlaceActivityMapper wrappedActivityMapper, AcceptsOneWidget wrappedDisplay) {
        this.wrappedActivityMapper = wrappedActivityMapper;
        this.wrappedActivityMapper.setPlaceController(this.wrappedPlaceController);
        ActivityManager wrappedActivityManager = new ActivityManager(this.wrappedActivityMapper, eventBus);
        wrappedActivityManager.setDisplay(wrappedDisplay);
    }

    public void start() {
        this.wrappedPlaceController.goTo(this.wrappedActivityMapper.getStartPlace());
    }
    
    public void goTo(Place newPlace) {
        this.wrappedPlaceController.goTo(newPlace);
    }

    public void fireEvent(UserManagementContextEvent event) {
        eventBus.fireEvent(event);
    }
    
}
