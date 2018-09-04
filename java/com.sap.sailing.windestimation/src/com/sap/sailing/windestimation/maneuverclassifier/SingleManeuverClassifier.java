package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface SingleManeuverClassifier extends ManeuverClassifier {

    double[] classifyManeuverWithProbabilities(ManeuverForEstimation maneuver);

    double getTestScore();

    ManeuverTypeForClassification getManeuverTypeByMappingIndex(int mappingIndex);

    List<ManeuverTypeForClassification> getSupportedManeuverTypes();

    int getSupportedManeuverTypesCount();

    int[] getSupportedManeuverTypesMapping();

    boolean isSupportsManeuverType(ManeuverTypeForClassification maneuverType);

    ManeuverFeatures getManeuverFeatures();

    BoatClass getBoatClass();

}
