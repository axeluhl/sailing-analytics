package com.sap.sailing.gwt.common.settings.converter;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class WindSourceTypeStringToEnumConverter implements StringToEnumConverter<WindSourceType> {

    @Override
    public WindSourceType fromString(String stringValue) {
        return WindSourceType.valueOf(stringValue);
    }

}
