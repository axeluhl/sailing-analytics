package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.shared.app.SailingActivityManager;

public class DesktopActivityManager extends SailingActivityManager<DesktopResettableNavigationPathDisplay> {

    public DesktopActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
    }

    @Override
    protected void afterNavigationPathIsSetToActivity(Activity activity) {
        super.afterNavigationPathIsSetToActivity(activity);
        if(activity instanceof WithHeader) {
            getNavigationPathDisplay().setWithHeader(true);
        }
    }
}
