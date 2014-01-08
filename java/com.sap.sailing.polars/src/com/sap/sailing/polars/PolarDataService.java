package com.sap.sailing.polars;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.data.PolarFix;

/**
 * Public Facade interface allowing access to the polars of {@link TrackedRace}s and per {@link BoatClass}. Methods like
 * {@link PolarDataService#getOptimalUpwindSpeedWithBearingFor(BoatClass, Speed)} allow the extraction of typical useful
 * information obtainable from polar sheets.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public interface PolarDataService {

    /**
     * Queries the {@link PolarDataService} for the optimal upwind speed and angle as calculated from the existing
     * database.
     * 
     * @param boatClass
     * @return The optimal upwind speed of the given boat class with the corresponding bearing difference to the wind.
     *         Bear in mind that the Bearing difference is an absolute. It can be applied to both sides of the wind.
     */
    SpeedWithBearing getOptimalUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed);

    /**
     * Queries the {@link PolarDataService} for the optimal downwind speed and angle as calculated from the existing
     * database.
     * 
     * @param boatClass
     * @return The optimal downwind speed of the given boat class with the corresponding bearing difference to the wind.
     *         Bear in mind that the Bearing difference is an absolute. It can be applied to both sides of the wind.
     */
    SpeedWithBearing getOptimalDownwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed);

    /**
     * Queries the {@link PolarDataService} for the optimal reaching speed at a given bearing difference to the wind.
     * This should be used for gathering information about non-up&down-wind legs. For up- and downwind legs use
     * {@link #getOptimalUpwindSpeedWithBearingFor(BoatClass, Speed)} and
     * {@link #getOptimalDownwindSpeedWithBearingFor(BoatClass, Speed)}.
     * 
     * @param boatClass
     * @param bearingDifferenceToWind
     *            absolute bearing difference of the boat's course to the wind
     * @return The optimal speed of the given boat class at the given bearing difference to the wind.
     */
    Speed getOptimalReachingSpeedFor(BoatClass boatClass, Speed windSpeed, Bearing bearingDifferenceToWind);

    /**
     * Queries the {@link PolarDataService} for the average upwind speed and angle as calculated from the existing
     * database.
     * 
     * @param boatClass
     * @return The average upwind speed of the given boat class with the corresponding bearing difference to the wind.
     *         Bear in mind that the Bearing difference is an absolute. It can be applied to both sides of the wind.
     */
    SpeedWithBearing getAverageUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed);

    /**
     * Queries the {@link PolarDataService} for the average downwind speed and angle as calculated from the existing
     * database.
     * 
     * @param boatClass
     * @return The average downwind speed of the given boat class with the corresponding bearing difference to the wind.
     *         Bear in mind that the Bearing difference is an absolute. It can be applied to both sides of the wind.
     */
    SpeedWithBearing getAverageDownwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed);

    /**
     * Queries the {@link PolarDataService} for the average reaching speed at a given bearing difference to the wind.
     * This should be used for gathering information about non-up&down-wind legs. For up- and downwind legs use
     * {@link #getAverageUpwindSpeedWithBearingFor(BoatClass, Speed)} and
     * {@link #getAverageDownwindSpeedWithBearingFor(BoatClass, Speed)}.
     * 
     * @param boatClass
     * @param bearingDifferenceToWind
     *            absolute bearing difference of the boat's course to the wind
     * @return The average speed of the given boat class at the given bearing difference to the wind.
     */
    Speed getAverageReachingSpeedFor(BoatClass boatClass, Speed windSpeed, Bearing bearingDifferenceToWind);

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
    public PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass);

}
