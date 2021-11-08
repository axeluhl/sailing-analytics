package com.sap.sailing.windestimation.util;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;

/**
 * Util class for diverse adjustments of lists with maneuvers
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverUtil {

    private ManeuverUtil() {
    }

    public static List<ManeuverWithEstimatedType> getManeuverWithEstimatedTypeWithFixedConfidence(
            List<ManeuverWithEstimatedType> maneuversWithEstimatedTypes, double fixedConfidence) {
        List<ManeuverWithEstimatedType> result = new ArrayList<>();
        for (ManeuverWithEstimatedType maneuverWithEstimatedType : maneuversWithEstimatedTypes) {
            ManeuverWithEstimatedType newManeuverWithEstimatedType = new ManeuverWithEstimatedType(
                    maneuverWithEstimatedType.getManeuver(), maneuverWithEstimatedType.getManeuverType(),
                    fixedConfidence);
            result.add(newManeuverWithEstimatedType);
        }
        return result;
    }

}
