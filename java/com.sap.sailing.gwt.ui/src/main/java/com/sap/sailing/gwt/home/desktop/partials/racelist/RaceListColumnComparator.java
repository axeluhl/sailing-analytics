package com.sap.sailing.gwt.home.desktop.partials.racelist;

import java.util.List;

import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;

/**
 * Special Comparator with following precedences:
 * <ol>
 * <li>The value of the column</li>
 * <li>Natural order of races</li>
 * <li>Race names</li>
 * </ol>
 *
 * Following constrains apply to the sorting order:
 * <ul>
 * <li>If a sorting value is null/empty, it gets always sorted below the non-empty values</li>
 * <li>The presendences 2) und 3) for sorting between empty values still apply</li>
 * <li>Ascending and descending order is also considered for empty values</li>
 * </ul>
 * 
 * @author Vladislav Chumak
 *
 * @param <R>
 *            DTO type
 * @param <S>
 *            type of the value to be sorted
 */
public abstract class RaceListColumnComparator<R extends RaceMetadataDTO<?>, S extends Comparable<S>>
        extends InvertibleComparatorAdapter<R> {

    private List<R> racesInNaturalOrder = null;

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
        if (compareResult == 0 && racesInNaturalOrder != null) {
            compareResult = Integer.compare(racesInNaturalOrder.indexOf(o1), racesInNaturalOrder.indexOf(o2));
        }
        
        if (compareResult == 0) {
            // RaceListRaceDTO implements Comparable which considers race names for its comparison
            compareResult = o1.compareTo(o2);
        }

        return compareResult;
    }

    public abstract S getValue(R object);

    /**
     * Sets the comparator to use the order of the passed {@link List} as second comparison criteria.
     * 
     * @param racesInNaturalOrder
     *            The list with items which order should be considered as second comparison criteria
     */
    public void setRacesInNaturalOrder(List<R> racesInNaturalOrder) {
        this.racesInNaturalOrder = racesInNaturalOrder;
    }
}
