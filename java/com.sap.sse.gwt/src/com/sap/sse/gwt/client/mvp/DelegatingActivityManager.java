package com.sap.sse.gwt.client.mvp;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * Custom {@link ActivityManager} implementation that allows hooking into the lifecycle.
 *
 */
public class DelegatingActivityManager extends ActivityManager {
    
    private final ActivityManager realActivityManager;

    public DelegatingActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(new NoOpActivityMapper(), new SimpleEventBus());
        realActivityManager = new ActivityManager(new ActivityMapperDelegate(mapper), eventBus);
    }
    
    public EventBus getActiveEventBus() {
        return realActivityManager.getActiveEventBus();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        realActivityManager.onPlaceChange(event);
    }

    public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
        realActivityManager.onPlaceChangeRequest(event);
    }

    public void setDisplay(AcceptsOneWidget display) {
        realActivityManager.setDisplay(display);
    }
    
    /**
     * To be overwritten by sublcasses to hook into the lifecycle.
     * 
     * @param activity the activity to be started
     */
    protected void beforeActivityStart(Activity activity) {
    }
    
    /**
     * To be overwritten by sublcasses to hook into the lifecycle.
     * 
     * @param activity the place that has been started
     */
    protected void afterPlaceActivation(Place place) {
    }

    private static class NoOpActivityMapper implements ActivityMapper {
        @Override
        public Activity getActivity(Place place) {
            return null;
        }
    }
    
    private final class ActivityMapperDelegate implements ActivityMapper {
        private final ActivityMapper realMapper;
        public ActivityMapperDelegate(ActivityMapper realMapper) {
            super();
            this.realMapper = realMapper;
        }

        @Override
        public Activity getActivity(Place place) {
            return new ActivityDelegate(place, realMapper.getActivity(place));
        }
    }
    
    private final class ActivityDelegate implements Activity {
        
        private final Activity realActivity;
        private final Place place;
        
        public ActivityDelegate(Place place, Activity realActivity) {
            this.place = place;
            this.realActivity = realActivity;
        }
        
        @Override
        public String mayStop() {
            return realActivity.mayStop();
        }
        
        @Override
        public void onCancel() {
            realActivity.onCancel();
        }
        
        @Override
        public void onStop() {
            realActivity.onStop();
        }
        
        @Override
        public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
            beforeActivityStart(realActivity);
            realActivity.start(panel, eventBus);
            afterPlaceActivation(place);
        }
    }
}
