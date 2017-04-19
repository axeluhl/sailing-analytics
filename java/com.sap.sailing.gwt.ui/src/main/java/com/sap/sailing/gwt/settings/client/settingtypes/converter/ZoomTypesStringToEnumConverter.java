package com.sap.sailing.gwt.settings.client.settingtypes.converter;

import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class ZoomTypesStringToEnumConverter implements StringToEnumConverter<ZoomTypes> {

    @Override
    public ZoomTypes fromString(String stringValue) {
        return ZoomTypes.valueOf(stringValue);
    }

}
