package com.sap.sailing.windestimation.maneuverclassifier;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.RandomForestManeuverClassifier;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.SVMManeuverClassifier;

public class ManeuverClassifiersFactory {

    public static final ManeuverTypeForClassification[] supportedManeuverTypes = { ManeuverTypeForClassification.TACK,
            ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.OTHER };

    private ManeuverClassifiersFactory() {
    }

    public static TrainableSingleManeuverOfflineClassifier getNewClassifierInstance(ManeuverFeatures maneuverFeatures,
            BoatClass boatClass) {
        if (boatClass == null) {
            return new RandomForestManeuverClassifier(maneuverFeatures, null, supportedManeuverTypes);
        } else {
            return new SVMManeuverClassifier(maneuverFeatures, boatClass, supportedManeuverTypes);
        }
    }

}
