package com.sap.sailing.polars;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

/**
 * Public Facade interface granting clients access to the polar sheets of {@link BoatClass}es. A boat's "polar sheet"
 * (sometimes also referred to as a "VPP" (velocity prediction program)) makes a prediction how fast the boat will
 * sail at a given true wind angle and a given true wind speed.<p>
 * 
 * This service uses a {@link PolarSheetAnalyzer} for more advanced analysis. Its methods are facaded in this interface for
 * central access.<p>
 * The interesting methods for a client are {@link #getSpeed(BoatClass, Speed, Bearing, boolean)} if data for a specific angle is
 * needed and {@link #getAverageSpeedWithBearing(BoatClass, Speed, LegType, Tack)} 
 * which also returns the average angle for the parameters provided.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public interface PolarDataService {

    /**
     * 
     * @param boatClass
     * @param windSpeed
     * @param trueWindAngle
     *            Boat's direction relative to the wind. either in -180 -> +180 or 0 -> 359 degrees interval. The true wind!
     * @return The speed the boat is moving at for the specified wind and bearing according to the polar diagram.
     * @throws NotEnoughDataHasBeenAddedException
     */
    SpeedWithConfidence<Void> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing trueWindAngle)
            throws NotEnoughDataHasBeenAddedException;
    
    /**
     * 
     * @param boatClass
     * @param windSpeed
     * @param legType
     *            Should be {@link LegType#UPWIND} or {@link LegType#DOWNWIND}, there is no information for other
     *            courses yet. Use getSpeed for the desired angle to get rawer information on other courses for now.
     * @param tack
     *            Polar data can vary depending on the tack the boat is on.
     * @return The estimated average speed of a boat for the supplied parameters with the estimated average bearing to
     *         the true wind and a confidence which consists of the confidences of the wind speed, and boat speed sources (50%)
     *         and a confidence calculated using the amount of underlying fixes (50%). 0 <= confidence < 1<br/>
     *         A value with zero confidence doesn't have any significance!<br/>
     * <br/>
     * 
     *         The bearing is somewhere between -179 to +180<br/>
     * <br/>
     * 
     *         Get the speed using returnValue.getObject()<br/>
     * <br/>
     * 
     *         Returns null if the leg type is not up or downwind.
     * 
     * @throws NotEnoughDataHasBeenAddedException
     *             If there is not enough data to supply a value with some kind of significance.
     */
    SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass, Speed windSpeed,
            LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException;


    /**
     * Generates a polar sheet for given races and settings using the provided executor for the worker threads. This
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

    /**
     * To be called in an appropriate listener. 
     * Starting point for fixes entering the backend polar data mining pipeline.
     * 
     * @param fix
     * @param competitor
     * @param createdTrackedRace
     */
    void competitorPositionChanged(GPSFixMoving fix, Competitor competitor, TrackedRace createdTrackedRace);

    /**
     * Returns underlying datacount for a given boat class and windspeed. 
     * @param boatClass
     * @param windSpeed
     * @param startAngleInclusive between 0 and 359; smaller than (or equal to) endAngleExclusive
     * @param endAngleExclusive between 0 and 359; bigger than startAngleInclusive
     * @return array with datacount for all angles in the given area, else -1
     */
    int[] getDataCountsForWindSpeed(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive);

    /**
     * From a boat's speed over ground and assuming values for <code>boatClass</code>, the <code>tack</code> the boat is
     * currently sailing on, and the <code>legType</code>, this method estimates the true wind speed at which the boat
     * may most likely have been sailing under these conditions.
     * 
     * As opposed to {@link #getAverageTrueWindSpeedAndAngleCandidates(BoatClass, Speed, LegType, Tack)} it only returns
     * the candidate with the highest confidence, which is derived from the average confidence of the underlying fixes,
     * distance measures and the amount of underlying data.
     * 
     * @return wind candidate with highest confidence of all candidates (if function is non-reversible), null if none
     *         was found (due to insufficient underlying data)
     */
    SpeedWithBearingWithConfidence<Void> getAverageTrueWindSpeedAndAngle(BoatClass boatClass, Speed speedOverGround, LegType legType, Tack tack);

    /**
     * From a boat's speed over ground and assuming values for <code>boatClass</code>, the <code>tack</code> the boat is
     * currently sailing on, and the <code>legType</code>, this method estimates the true wind speed candidates at which
     * the boat may most likely have been sailing under these conditions.
     * 
     * The confidence of the returned candidates is derived from the average confidence of the underlying fixes,
     * distance measures and the amount of underlying data.
     * 
     * Multiple candidates are possible, because we cannot guarantee a reversible function (boatspeed over windspeed).
     * 
     * @return set of wind candidates with confidence, empty set if no were found (due to insufficient underlying data)
     */
    Set<SpeedWithBearingWithConfidence<Void>> getAverageTrueWindSpeedAndAngleCandidates(BoatClass boatClass, Speed speedOverGround, LegType legType, Tack tack);

}
