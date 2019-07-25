package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

/**
 * Predicate to test whether a maneuver is eligible for wind estimation use.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverFilteringImpl implements DataFilteringPredicate<ManeuverForEstimation> {

    @Override
    public boolean test(ManeuverForEstimation maneuver) {
        return maneuver.isClean() && (maneuver.getManeuverCategory() == ManeuverCategory.REGULAR
                || maneuver.getManeuverCategory() == ManeuverCategory.MARK_PASSING);
    }

}
