package com.sap.sailing.gwt.common.settings.converter;

import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class HelpLineTypesStringToEnumConverter implements StringToEnumConverter<HelpLineTypes> {

    @Override
    public HelpLineTypes fromString(String stringValue) {
        return HelpLineTypes.valueOf(stringValue);
    }

}
