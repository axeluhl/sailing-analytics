package com.sap.sailing.gwt.common.settings.converter;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sse.common.settings.generic.StringToEnumConverter;

public class RaceColumnSelectionStrategiesStringToEnumConverter implements StringToEnumConverter<RaceColumnSelectionStrategies> {

    @Override
    public RaceColumnSelectionStrategies fromString(String stringValue) {
        return RaceColumnSelectionStrategies.valueOf(stringValue);
    }

}
