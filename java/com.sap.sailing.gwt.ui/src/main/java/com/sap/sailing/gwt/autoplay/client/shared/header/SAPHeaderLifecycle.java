package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.ComponentConstructionParameters;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SAPHeaderLifecycle implements ComponentLifecycle<SAPHeader, AbstractSettings, SettingsDialogComponent<AbstractSettings>, SAPHeaderLifecycle.SAPHeaderConstructorArgs> {

    public static class SAPHeaderConstructionParameters extends ComponentConstructionParameters<SAPHeader, AbstractSettings, SettingsDialogComponent<AbstractSettings>, SAPHeaderLifecycle.SAPHeaderConstructorArgs> {
        public SAPHeaderConstructionParameters(SAPHeaderLifecycle componentLifecycle,
                SAPHeaderConstructorArgs componentConstructorArgs, AbstractSettings settings) {
            super(componentLifecycle, componentConstructorArgs, settings);
        }
    }

    public SAPHeaderLifecycle() {
    }
    
    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent(AbstractSettings settings) {
        return null;
    }

    @Override
    public AbstractSettings createDefaultSettings() {
        return null;
    }

    @Override
    public AbstractSettings cloneSettings(AbstractSettings settings) {
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return "Header";
    }

    @Override
    public boolean hasSettings() {
        return false;
    }
    
    @Override
    public SAPHeader createComponent(SAPHeaderConstructorArgs sapHeaderContructorArgs, AbstractSettings settings) {
        return sapHeaderContructorArgs.createComponent(settings);
    }

    public static class SAPHeaderConstructorArgs implements ComponentConstructorArgs<SAPHeader, AbstractSettings> {
        private final String pageTitle;
        private final boolean startInAutoScreenMode;
        
        public SAPHeaderConstructorArgs(String pageTitle, boolean startInAutoScreenMode) {
            this.pageTitle = pageTitle;
            this.startInAutoScreenMode = startInAutoScreenMode;
        }
        
        @Override
        public SAPHeader createComponent(AbstractSettings newSettings) {
            return new SAPHeader(pageTitle, startInAutoScreenMode);
        }
    }

}

