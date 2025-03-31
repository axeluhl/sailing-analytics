package com.sap.sailing.gwt.settings.client.settingtypes;

import com.sap.sailing.gwt.settings.client.settingtypes.converter.DistanceConverter;
import com.sap.sse.common.Distance;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.base.AbstractValueSetting;

public class DistanceSetting extends AbstractValueSetting<Distance> {
    public DistanceSetting(String name, AbstractGenericSerializableSettings settings) {
        this(name, settings, null);
    }
    
    public DistanceSetting(String name, AbstractGenericSerializableSettings settings, Distance defaultValue) {
        super(name, settings, defaultValue, DistanceConverter.INSTANCE);
    }

}
