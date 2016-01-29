package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentConstructionParameters;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class SAPHeaderLifecycle implements ComponentLifecycle<SAPHeader, SAPHeaderSettings, SAPHeaderSettingsDialogComponent, SAPHeaderLifecycle.ConstructorArgs> {
    private final StringMessages stringMessages;
    private final String defaultTitle;
    
    public static class ConstructionParameters extends ComponentConstructionParameters<SAPHeader, SAPHeaderSettings, SAPHeaderSettingsDialogComponent, SAPHeaderLifecycle.ConstructorArgs> {
        public ConstructionParameters(SAPHeaderLifecycle componentLifecycle,
                ConstructorArgs componentConstructorArgs, SAPHeaderSettings settings) {
            super(componentLifecycle, componentConstructorArgs, settings);
        }
    }

    public SAPHeaderLifecycle(String defaultTitle, StringMessages stringMessages) {
        this.defaultTitle = defaultTitle;
        this.stringMessages = stringMessages;
    }
    
    @Override
    public SAPHeaderSettingsDialogComponent getSettingsDialogComponent(SAPHeaderSettings settings) {
        return new SAPHeaderSettingsDialogComponent(cloneSettings(settings), stringMessages);
    }

    @Override
    public SAPHeaderSettings createDefaultSettings() {
        return new SAPHeaderSettings(defaultTitle);
    }

    @Override
    public SAPHeaderSettings cloneSettings(SAPHeaderSettings settings) {
        return new SAPHeaderSettings(settings.getTitle());
    }

    @Override
    public String getLocalizedShortName() {
        return "SAP Header";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public SAPHeader createComponent(ConstructorArgs sapHeaderContructorArgs, SAPHeaderSettings settings) {
        return sapHeaderContructorArgs.createComponent(settings);
    }

    public static class ConstructorArgs implements ComponentConstructorArgs<SAPHeader, SAPHeaderSettings> {
        private final boolean startInAutoScreenMode;
        private final SAPHeaderLifecycle componentLifecycle;
        
        public ConstructorArgs(SAPHeaderLifecycle componentLifecycle, boolean startInAutoScreenMode) {
            this.componentLifecycle = componentLifecycle;
            this.startInAutoScreenMode = startInAutoScreenMode;
        }
        
        @Override
        public SAPHeader createComponent(SAPHeaderSettings settings) {
            return new SAPHeader(componentLifecycle, settings, startInAutoScreenMode);
        }
    }

}

