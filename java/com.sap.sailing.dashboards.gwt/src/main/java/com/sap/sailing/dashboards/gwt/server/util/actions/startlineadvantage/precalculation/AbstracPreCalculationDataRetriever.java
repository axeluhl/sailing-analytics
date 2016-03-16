package com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.precalculation;

import com.sap.sailing.domain.base.DomainFactory;
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
    private StartlineAndFirstMarkPositions startlineAndFirstMarkPositions;
    private double startlineAdvantageAtPinEndInMeters;
    private double startlineLenghtInMeters;
    private Wind wind;
    private double maneuverAngle;
    
    private final DomainFactory domainFactory;
    
    public AbstracPreCalculationDataRetriever(DomainFactory domainFactory) {
        super();
        this.domainFactory = domainFactory;
    }

    protected void retrieveDataForCalculation(TrackedRace trackedRace, PolarDataService polarDataService) {
        startlineAndFirstMarkPositions = retrieveMarkPositions(trackedRace);
        startlineAdvantageAtPinEndInMeters = retrieveStartlineAdvantage(trackedRace);
        startlineLenghtInMeters = retrieveStartlineLenght(trackedRace);
        wind = retrieveWindAtPosition(startlineAndFirstMarkPositions.startBoatPosition, trackedRace);
        maneuverAngle = retrieveManouvreAngleAtWindSpeedAndBoatClass(domainFactory.getOrCreateBoatClass("Extreme 40"), ManeuverType.TACK, wind, polarDataService);
    }

    protected StartlineAndFirstMarkPositions getStartlineAndFirstMarkPositions() {
        return startlineAndFirstMarkPositions;
    }

    protected double getStartlineAdvantageAtPinEndInMeters() {
        return startlineAdvantageAtPinEndInMeters;
    }

    protected double getStartlineLenghtInMeters() {
        return startlineLenghtInMeters;
    }

    protected Wind getWind() {
        return wind;
    }

    protected double getManeuverAngle() {
        return maneuverAngle;
    }
    
    
}
