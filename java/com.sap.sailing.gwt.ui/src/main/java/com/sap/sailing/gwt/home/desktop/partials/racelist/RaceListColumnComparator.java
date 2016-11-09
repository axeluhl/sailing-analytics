package com.sap.sailing.gwt.home.desktop.partials.racelist;

import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;

public abstract class RaceListColumnComparator<R extends RaceMetadataDTO<?>, S extends Comparable<S>>
        extends InvertibleComparatorAdapter<R> {

    @Override
    public int compare(R o1, R o2) {
        int compareResult;
        if (getValue(o1) == getValue(o2)) {
            compareResult = 0;
        } else if (getValue(o1) == null) {
            return isAscending() ? 1 : -1;
        } else if (getValue(o2) == null) {
            return isAscending() ? -1 : 1;
        } else {
            compareResult = getValue(o1).compareTo(getValue(o2));
        }
        if (compareResult == 0) {
            //RaceListRaceDTO implements Comparable which considers natural order and race names for its comparison
            compareResult = o1.compareTo(o2);
        }

        return compareResult;
    }

    public abstract S getValue(R object);
}
