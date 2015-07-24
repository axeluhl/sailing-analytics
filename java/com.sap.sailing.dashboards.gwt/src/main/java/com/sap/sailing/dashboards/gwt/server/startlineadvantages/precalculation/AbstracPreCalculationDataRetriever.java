package com.sap.sailing.dashboards.gwt.server.startlineadvantages.precalculation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.server.startlineadvantages.StartlineAdvantagesCalculator;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Wind;
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
    
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesCalculator.class.getName());

    protected void retrieveDataForCalculation(TrackedRace trackedRace) {
        startlineAndFirstMarkPositions = retrieveMarkPositions(trackedRace);
        startlineAdvantageAtPinEndInMeters = retrieveStartlineAdvantage(trackedRace);
        startlineLenghtInMeters = retrieveStartlineLenght(trackedRace);
        wind = retrieveWindAtPosition(startlineAndFirstMarkPositions.startBoatPosition, trackedRace);
        meouvreAngle = retrieveManouvreAngleAtWindSpeedAndBoatClass(new BoatClassImpl("Extreme40", BoatClassMasterdata.EXTREME_40), ManeuverType.TACK, wind);
        printData();
    }
    
    private void printData() {
        logger.log(Level.INFO, "Retrieved pre calculation data for startline advantages calculation");
        logger.log(Level.INFO, "startlineAndFirstMarkPositions.startBoatPosition "+startlineAndFirstMarkPositions.startBoatPosition);
        logger.log(Level.INFO, "startlineAndFirstMarkPositions.pinEndPosition "+startlineAndFirstMarkPositions.pinEndPosition);
        logger.log(Level.INFO, "startlineAndFirstMarkPositions.firstMarkPosition "+startlineAndFirstMarkPositions.firstMarkPosition);
        logger.log(Level.INFO, "startlineAdvantageAtPinEndInMeters "+startlineAdvantageAtPinEndInMeters);
        logger.log(Level.INFO, "startlineLenghtInMeters "+startlineLenghtInMeters);
        logger.log(Level.INFO, "wind.getBeaufort() "+wind.getKnots());
        logger.log(Level.INFO, "wind.getBearing().getDegrees() "+wind.getBearing().getDegrees());
        logger.log(Level.INFO, "meouvreAngle.getDegrees() "+meouvreAngle);
    }
}
