package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.FullscreenUtil;
import com.sap.sse.gwt.client.shared.components.Component;

public class SAPHeader extends Composite implements Component<SAPHeaderSettings> {
    private static SAPHeaderUiBinder uiBinder = GWT.create(SAPHeaderUiBinder.class);

    private SAPHeaderSettings settings;
    private final SAPHeaderLifecycle componentLifecycle;
    
    interface SAPHeaderUiBinder extends UiBinder<Widget, SAPHeader> {
    }

    @UiField DivElement pageTitleDiv;
    @UiField Button startFullScreenButton;
    
    public SAPHeader(SAPHeaderLifecycle componentLifecycle, SAPHeaderSettings settings, boolean startInAutoScreenMode) {
        this.componentLifecycle = componentLifecycle;
        this.settings = settings;
        
        SAPHeaderResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageTitleDiv.setInnerText(settings.getTitle());
        startFullScreenButton.setVisible(startInAutoScreenMode);

        // the 'fullscreen' button should disappear after some seconds (10)
        if(startInAutoScreenMode) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    startFullScreenButton.setVisible(false);
                    return false;
                }
              }, 10000);
        }
    }
    
   @UiHandler("startFullScreenButton") 
   void startFullScreenClicked(ClickEvent e) {
       FullscreenUtil.requestFullscreen();
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
    public SAPHeaderSettingsDialogComponent getSettingsDialogComponent() {
        return componentLifecycle.getSettingsDialogComponent(settings);
    }
    
    @Override
    public SAPHeaderSettings getSettings() {
        return settings;
    }
    
    @Override
    public void updateSettings(SAPHeaderSettings newSettings) {
        // no-op
    }
    
    @Override
    public String getDependentCssClassName() {
        return "";
    }
}
