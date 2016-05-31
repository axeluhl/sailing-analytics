package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.sapheader.SAPHeader;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;

public class SAPHeaderComponent extends AbstractCompositeComponent<SAPHeaderComponentSettings> {
    private SAPHeaderComponentSettings settings;
    private final SAPHeaderComponentLifecycle componentLifecycle;
    
    private final SAPHeader sapHeader;
    
    public SAPHeaderComponent(SAPHeaderComponentLifecycle componentLifecycle, SAPHeaderComponentSettings settings, 
            StringMessages stringMessages, boolean startInAutoScreenMode) {
        this.componentLifecycle = componentLifecycle;
        this.settings = settings;
        this.sapHeader = new SAPHeader(stringMessages.sapSailingAnalytics());
        
        sapHeader.setHeaderTitle(settings.getTitle());
        initWidget(sapHeader);
        
        if(startInAutoScreenMode) {
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
