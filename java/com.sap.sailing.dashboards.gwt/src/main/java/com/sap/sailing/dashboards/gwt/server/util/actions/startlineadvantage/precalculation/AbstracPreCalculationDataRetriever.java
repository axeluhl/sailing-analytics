package com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.precalculation;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * @author Alexander Ries (D062114)
 *
 */
public abstract class AbstracPreCalculationDataRetriever implements PreCalculationMarkRetriever,
                                                                    PreCalculationStartlineAdvantageRetriever, 
                                                                    PreCalculationWindRetriever, 
                                                                    PreCalculationPolarDataRetriever {
    public StartlineAndFirstMarkPositions startlineAndFirstMarkPositions;
    public double startlineAdvantageAtPinEndInMeters;
    public double startlineLenghtInMeters;
    public Wind wind;
    public double meouvreAngle;
    
    protected void retrieveDataForCalculation(TrackedRace trackedRace, PolarDataService polarDataService) {
        startlineAndFirstMarkPositions = retrieveMarkPositions(trackedRace);
        startlineAdvantageAtPinEndInMeters = retrieveStartlineAdvantage(trackedRace);
        startlineLenghtInMeters = retrieveStartlineLenght(trackedRace);
        wind = retrieveWindAtPosition(startlineAndFirstMarkPositions.startBoatPosition, trackedRace);
        meouvreAngle = retrieveManouvreAngleAtWindSpeedAndBoatClass(new BoatClassImpl("Extreme40", BoatClassMasterdata.EXTREME_40), ManeuverType.TACK, wind, polarDataService);
    }
}
