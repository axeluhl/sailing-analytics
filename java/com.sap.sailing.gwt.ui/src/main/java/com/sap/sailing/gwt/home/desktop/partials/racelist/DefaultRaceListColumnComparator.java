package com.sap.sailing.gwt.home.desktop.partials.racelist;

import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;

public class DefaultRaceListColumnComparator<T extends RaceMetadataDTO<?>> extends RaceListColumnComparator<T, String> {
    /**
     * Returns {@code null} for all objects passed, forcing the {@link #compare(RaceMetadataDTO, RaceMetadataDTO)} method
     * to resort to {@link RaceMetadataDTO#getNaturalOrder()} for comparison.
     */
    @Override
    public String getValue(T object) {
        return null;
    }

}
