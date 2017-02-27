package com.sap.sailing.gwt.settings.client.settingtypes.converter;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class ManeuverTypeStringToEnumConverter implements StringToEnumConverter<ManeuverType> {

    @Override
    public ManeuverType fromString(String stringValue) {
        return ManeuverType.valueOf(stringValue);
    }

}
