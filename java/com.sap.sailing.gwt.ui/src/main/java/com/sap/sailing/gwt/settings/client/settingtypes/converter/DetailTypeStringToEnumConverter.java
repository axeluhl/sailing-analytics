package com.sap.sailing.gwt.settings.client.settingtypes.converter;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class DetailTypeStringToEnumConverter implements StringToEnumConverter<DetailType> {

    @Override
    public DetailType fromString(String stringValue) {
        return DetailType.valueOf(stringValue);
    }

}
