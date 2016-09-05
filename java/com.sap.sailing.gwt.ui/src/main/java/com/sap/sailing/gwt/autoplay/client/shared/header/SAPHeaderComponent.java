package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;
import com.sap.sse.security.ui.client.UserService;

public class SAPHeaderComponent extends AbstractCompositeComponent<SAPHeaderComponentSettings> {
    private SAPHeaderComponentSettings settings;
    private final SAPHeaderComponentLifecycle componentLifecycle;
    
    private final SAPHeaderWithAuthentication sapHeader;
    
    public SAPHeaderComponent(SAPHeaderComponentLifecycle componentLifecycle, UserService userService, SAPHeaderComponentSettings settings, 
            StringMessages stringMessages, boolean startInAutoScreenMode) {
        this.componentLifecycle = componentLifecycle;
        this.settings = settings;
        this.sapHeader = new SAPHeaderWithAuthentication(stringMessages.sapSailingAnalytics(), settings.getTitle());
        new FixedSailingAuthentication(userService, sapHeader.getAuthenticationMenuView());

        initWidget(sapHeader);
        
        if (startInAutoScreenMode) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    FullscreenUtil.requestFullscreen();
                    return false;
                }
              }, 1000);
        }
    }
    
    @Override
    public String getLocalizedShortName() {
        return componentLifecycle.getLocalizedShortName();
    }
    
    @Override
    public Widget getEntryWidget() {
        return this;
    }
    
    @Override
    public boolean hasSettings() {
        return componentLifecycle.hasSettings();
    }
    
    @Override
    public SAPHeaderComponentSettingsDialogComponent getSettingsDialogComponent() {
        return componentLifecycle.getSettingsDialogComponent(settings);
    }
    
    @Override
    public SAPHeaderComponentSettings getSettings() {
        return settings;
    }
    
    @Override
    public void updateSettings(SAPHeaderComponentSettings newSettings) {
        this.settings = newSettings;
        sapHeader.setHeaderTitle(settings.getTitle());
    }
    
    @Override
    public String getDependentCssClassName() {
        return "";
    }
}
