package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;

/** 
 * A perspective and it's settings 
 **/
public class PerspectiveAndSettingsPair<SettingsType extends Settings> extends ComponentAndSettings<SettingsType>  {
    private static final long serialVersionUID = -5647140233314161466L;

    public PerspectiveAndSettingsPair(Perspective<SettingsType> perspective, SettingsType settings) {
        super(perspective, settings);
    }

    @Override
    public Perspective<SettingsType> getComponent() {
        return (Perspective<SettingsType>) super.getComponent();
    }
}