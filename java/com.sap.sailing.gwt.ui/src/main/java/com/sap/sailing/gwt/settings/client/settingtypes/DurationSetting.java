package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DurationConverter;
import com.sap.sse.common.Duration;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;

public class DurationSetting extends AbstractValueSetting<Duration> {
    public DurationSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public DurationSetting(String name, AbstractGenericSerializableSettings settings, Duration defaultValue) {
        super(name, settings, defaultValue, DurationConverter.INSTANCE);
    }

}
