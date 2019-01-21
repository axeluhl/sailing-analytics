package com.sap.sailing.windestimation.data.transformer;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Wind;

public interface ConvertableToLabelledManeuverForEstimation extends ConvertableToManeuverForEstimation {

    Wind getWind();
    
    ManeuverType getManeuverTypeForCompleteManeuverCurve();

}
