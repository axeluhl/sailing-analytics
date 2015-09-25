package com.sap.sailing.dashboards.gwt.server.startlineadvantages.precalculation;

import com.sap.sailing.dashboards.gwt.server.startlineadvantages.DefaultPolarValues;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface PreCalculationPolarDataRetriever {

    default double retrieveManouvreAngleAtWindSpeedAndBoatClass(BoatClass boatClass, ManeuverType maneuverType,
            Speed windSpeed) {
        double result = 0;
        try {
            BearingWithConfidence<Void> bearingWithConfidence = getPolarDataService().getManeuverAngle(boatClass, maneuverType, windSpeed);
            result = bearingWithConfidence.getObject().getDegrees();
        } catch (NotEnoughDataHasBeenAddedException | NullPointerException e) {
            result = DefaultPolarValues.getManouvreAngle(ManeuverType.TACK);
            e.printStackTrace();
        }
        return result;
    }
    
    PolarDataService getPolarDataService();
}
