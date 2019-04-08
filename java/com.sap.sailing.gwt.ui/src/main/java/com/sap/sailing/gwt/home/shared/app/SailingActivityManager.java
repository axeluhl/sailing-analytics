package com.sap.sailing.gwt.home.shared.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.CustomActivityManager;

/**
 * Specific {@link CustomActivityManager} that controls a {@link ResettableNavigationPathDisplay} by resetting the
 * navigation path before activation of an activity. With that, there is no wrong path in the navigation path if an
 * acitivity defines a navigation path by itself.
 *
 * @param <NPD> the concrete {@link ResettableNavigationPathDisplay} used by this ActivityManager.
 */
public class SailingActivityManager<NPD extends ResettableNavigationPathDisplay> extends CustomActivityManager {

    private NPD navigationPathDisplay;
    private Activity currentActivity;

    public SailingActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
    }
    
    public void setNavigationPathDisplay(NPD navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }
    
    protected NPD getNavigationPathDisplay() {
        return navigationPathDisplay;
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
                afterNavigationPathIsSetToActivity(activity);
            }
        }
    }
    
    protected void afterNavigationPathIsSetToActivity(Activity activity) {
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
