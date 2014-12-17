package com.sap.sailing.polars;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

/**
 * Public Facade interface allowing access to the polars of {@link BoatClass}es.
 * 
 * It uses a {@link PolarSheetAnalyzer} for more advanced analysis. It's methods are facaded in this interface for
 * central access.
 * 
 * The interesting methods for a user are {@link #getSpeed(BoatClass, Speed, Bearing, boolean)} if data for a specific angle is
 * needed and {@link #getAverageSpeedWithBearing(BoatClass, Speed, LegType, Tack)} 
 *  which also returns the average angle for the provided parameters.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public interface PolarDataService {

    /**
     * 
     * @param boatClass
     * @param windSpeed
     * @param bearingToTheWind
     *            Boat's direction relative to the wind. either in -180 -> +180 or 0 -> 359 degrees interval
     * @return The speed the boat is moving at for the specified wind and bearing according to the polar diagram.
     * @throws NotEnoughDataHasBeenAddedException
     */
    SpeedWithConfidence<Void> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing bearingToTheWind)
            throws NotEnoughDataHasBeenAddedException;
    
    /**
     * 
     * @param boatClass
     * @param windSpeed 
     * @param legType
     *            Should be UpWind or DownWind, there is no information for other courses yet. Use getSpeed for the
     *            desired angle to get rawer information on other courses for now.
     * @param tack
     *            Polar data can vary depending on the tack the boat is on.
     * @return The estimated average speed of a boat for the supplied parameters with the estimated average bearing to
     *         the wind and a confidence which consists of the confidences of the wind speed, and boat speed sources (50%)
     *         and a confidence calculated using the amount of underlying fixes (50%). 0 <= confidence < 1<br/>
     *         A value with zero confidence doesn't have any significance!<br/><br/>
     *         
     *         The bearing is somewhere between -179 to +180<br/><br/>
     *         
     *         Get the speed using returnValue.getObject()<br/><br/>
     *         
     *         Returns null if the leg type is not up or downwind.
     *         
     * @throws NotEnoughDataHasBeenAddedException
     *             If there is not enough data to supply a value with some kind of significance.
     */
    SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass, Speed windSpeed,
            LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException;


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
    Set<BoatClassMasterdata> getAllBoatClassesWithPolarSheetsAvailable();

    void competitorPositionChanged(GPSFixMoving fix, Competitor competitor, TrackedRace createdTrackedRace);

    /**
     * Returns underlying datacount for a given boat class and windspeed. 
     * @param boatClass
     * @param windSpeed
     * @param startAngleInclusive between 0 and 359; smaller than (or equal to) endAngleExclusive
     * @param endAngleExclusive between 0 and 359; bigger than startAngleInclusive
     * @return array with datacount for all angles in the given area, else null
     */
    Integer[] getDataCountsForWindSpeed(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive);
    

}
