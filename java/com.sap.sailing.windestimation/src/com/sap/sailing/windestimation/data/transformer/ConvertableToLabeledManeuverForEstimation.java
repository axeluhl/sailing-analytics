package com.sap.sailing.windestimation.data.transformer;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Wind;

/**
 * Conversion helper class for {@link ManeuverForEstimationTransformer} to convert arbitrary instances implementing this
 * interface to {@link LabeledManeuverForEstimation}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ConvertableToLabeledManeuverForEstimation extends ConvertableToManeuverForEstimation {

    Wind getWind();

    ManeuverType getManeuverTypeForCompleteManeuverCurve();

}
