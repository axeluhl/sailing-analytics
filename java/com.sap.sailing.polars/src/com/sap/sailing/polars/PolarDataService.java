package com.sap.sailing.polars;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

/**
 * Public Facade interface allowing access to the polars of {@link TrackedRace}s and per {@link BoatClass}.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public interface PolarDataService extends PolarSheetAnalyzer {

    /**
     * 
     * @param boatClass
     * @param windSpeed
     * @param bearingToTheWind
     *            Boat's direction relative to the wind. either in -180 -> +180 or 0 -> 359 degrees
     * @return The speed the boat is moving at for the specified wind and bearing according to the polar diagram.
     * @throws NotEnoughDataHasBeenAddedException
     */
    SpeedWithConfidence<Void> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing bearingToTheWind)
            throws NotEnoughDataHasBeenAddedException;
    
    SpeedWithBearingWithConfidence<Void> getAverageUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException;

    SpeedWithBearingWithConfidence<Void> getAverageDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException;

    SpeedWithBearingWithConfidence<Void> getAverageUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException;

    SpeedWithBearingWithConfidence<Void> getAverageDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException;

    /**
     * Generates a polar sheet for geven races and settings using the provided executor for the worker threads. This
     * method does not access a cache for now.
     * 
     * @param trackedRaces
     *            The set of races to generate the diagram for.
     * @param settings
     *            Settings as supplied by the user.
     * @param executor
     *            The executor to run the worker threads with.
     * @return The generated polar sheet with meta data.
     */
    PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException;

    void newRaceFinishedTracking(TrackedRace trackedRace);

    /**
     * @param key
     *            The {@link BoatClass} to obtain fixes for.
     * @return All raw polar fixes for the {@link BoatClass}. The implementation is responsible for deciding wether a
     *         cache is used or not.
     */
    Set<PolarFix> getPolarFixesForBoatClass(BoatClass key);

    /**
     * 
     * @param boatClass
     *            The {@link BoatClass} to obtain the polar sheet for.
     * @return The polar sheet for all existing races of the {@link BoatClass}.
     */
    PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass);

    /**
     * 
     * @return The {@link BoatClass}es for which there are polar sheets available via
     *         {@link PolarDataService#getPolarSheetForBoatClass(BoatClass)}
     */
    Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable();

    void competitorPositionChanged(GPSFixMoving fix, Competitor competitor, TrackedRace createdTrackedRace);
    

}
