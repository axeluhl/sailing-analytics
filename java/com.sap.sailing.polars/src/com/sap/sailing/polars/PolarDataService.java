package com.sap.sailing.polars;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;

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

}
