package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverFilteringImpl implements DataFilteringPredicate<ManeuverForEstimation> {

    @Override
    public boolean test(ManeuverForEstimation maneuver) {
        return maneuver.isClean() && (maneuver.getManeuverCategory() == ManeuverCategory.REGULAR
                || maneuver.getManeuverCategory() == ManeuverCategory.MARK_PASSING);
    }

}
