package com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.precalculation;

import com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.DefaultPolarValues;
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

    default double retrieveManouvreAngleAtWindSpeedAndBoatClass(BoatClass boatClass, ManeuverType maneuverType, Speed windSpeed, PolarDataService polarDataService) {
        double result = 0;
        try {
            if (boatClass != null && maneuverType != null && windSpeed != null) {
                BearingWithConfidence<Void> bearingWithConfidence =  polarDataService.getManeuverAngle(boatClass, maneuverType, windSpeed);
                result = bearingWithConfidence.getObject().getDegrees();
            } else {
                result = DefaultPolarValues.getManouvreAngle(ManeuverType.TACK);
            }
        } catch (NotEnoughDataHasBeenAddedException | NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }
}
