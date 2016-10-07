package com.sap.sailing.gwt.home.desktop.partials.racelist;

import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;

public class DefaultRaceListColumnComparator<T extends RaceMetadataDTO<?>> extends RaceListColumnComparator<T, String> {

    @Override
    public String getValue(T object) {
        return null;
    }

}
