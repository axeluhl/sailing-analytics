package com.sap.sailing.gwt.home.shared.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.CustomActivityManager;

public class SailingActivityManager extends CustomActivityManager {

    private ResettableNavigationPathDisplay navigationPathDisplay;
    private Activity currentActivity;

    public SailingActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
    }
    
    public void setNavigationPathDisplay(ResettableNavigationPathDisplay navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }

    @Override
    protected void beforeActivityStart(Activity activity) {
        this.currentActivity = activity;
        super.beforeActivityStart(activity);
        if(navigationPathDisplay != null) {
            navigationPathDisplay.reset();
            if(activity instanceof ProvidesNavigationPath) {
                ProvidesNavigationPath providesNavigationPath = (ProvidesNavigationPath) activity;
                providesNavigationPath.setNavigationPathDisplay(new NavigationPathDisplayDelegate(activity, navigationPathDisplay));
            }
        }
    }
    
    private class NavigationPathDisplayDelegate implements NavigationPathDisplay {
        
        private final Activity associatedActivity;
        private final NavigationPathDisplay realNavigationPathDisplay;

        public NavigationPathDisplayDelegate(Activity associatedActivity, NavigationPathDisplay realNavigationPathDisplay) {
            this.associatedActivity = associatedActivity;
            this.realNavigationPathDisplay = realNavigationPathDisplay;
        }

        @Override
        public void showNavigationPath(NavigationItem... navigationPath) {
            if(associatedActivity == currentActivity) {
                realNavigationPathDisplay.showNavigationPath(navigationPath);
            }
        }
        
    }
}
