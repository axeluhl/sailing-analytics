package com.sap.sailing.gwt.settings.client.settingtypes.converter;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class RaceColumnSelectionStrategiesStringToEnumConverter implements StringToEnumConverter<RaceColumnSelectionStrategies> {

    @Override
    public RaceColumnSelectionStrategies fromString(String stringValue) {
        return RaceColumnSelectionStrategies.valueOf(stringValue);
    }

}
