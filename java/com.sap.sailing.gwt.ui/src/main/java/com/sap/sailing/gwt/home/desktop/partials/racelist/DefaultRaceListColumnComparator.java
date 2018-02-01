package com.sap.sailing.gwt.home.desktop.partials.racelist;

import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;

public class DefaultRaceListColumnComparator<T extends RaceMetadataDTO<?>> extends RaceListColumnComparator<T, String> {
    @Override
    public int compare(T o1, T o2) {
        return compareByNaturalOrder(o1, o2);
    }

    /**
     * This method is unused by the overridden {@link #compare(RaceMetadataDTO, RaceMetadataDTO)} implementation; still
     * it needs an implementation because the super class declares it as abstract.
     */
    @Override
    public String getValue(T object) {
        return null;
    }
}
