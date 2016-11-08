package com.sap.sailing.gwt.common.settings;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.gwt.common.settings.converter.DistanceConverter;
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
