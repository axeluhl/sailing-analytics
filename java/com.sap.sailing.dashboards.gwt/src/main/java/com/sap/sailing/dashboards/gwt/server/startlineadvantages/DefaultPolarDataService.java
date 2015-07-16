package com.sap.sailing.dashboards.gwt.server.startlineadvantages;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.polars.PolarsChangedListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util.Pair;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DefaultPolarDataService implements PolarDataService{

    @Override
    public SpeedWithConfidence<Void> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing trueWindAngle)
            throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass, Speed windSpeed,
            LegType legType, Tack tack, boolean useRegressionForSpeed) throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass) {
        return null;
    }

    @Override
    public Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable() {
        return null;
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor, TrackedRace createdTrackedRace) {
    }

    @Override
    public int[] getDataCountsForWindSpeed(BoatClass boatClass, Speed windSpeed, int startAngleInclusive,
            int endAngleExclusive) {
        return null;
    }

    @Override
    public Set<SpeedWithBearingWithConfidence<Void>> getAverageTrueWindSpeedAndAngleCandidates(BoatClass boatClass,
            Speed speedOverGround, LegType legType, Tack tack) {
        return null;
    }

    @Override
    public double getConfidenceForTackJibeSpeedRatio(Speed intoTackSpeed, Speed intoJibeSpeed, BoatClass boatClass) {
        return 0;
    }

    @Override
    public Pair<Double, SpeedWithBearingWithConfidence<Void>> getManeuverLikelihoodAndTwsTwa(BoatClass boatClass,
            Speed speedOverGround, double courseChangeDeg, ManeuverType maneuverType) {
        return null;
    }

    @Override
    public PolynomialFunction getSpeedRegressionFunction(BoatClass boatClass, LegType legType)
            throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public PolynomialFunction getAngleRegressionFunction(BoatClass boatClass, LegType legType)
            throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public PolynomialFunction getSpeedRegressionFunction(BoatClass boatClass, double trueWindAngle)
            throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public BearingWithConfidence<Void> getManeuverAngle(BoatClass boatClass, ManeuverType maneuverType, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public void raceFinishedLoading(TrackedRace race) {
        
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass, Speed windSpeed,
            LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException {
        return null;
    }

    @Override
    public void insertExistingFixes(TrackedRace trackedRace) {
        
    }

    @Override
    public void registerListener(BoatClass boatClass, PolarsChangedListener listener) {
        
    }

    @Override
    public void unregisterListener(BoatClass boatClass, PolarsChangedListener listener) {
    }
}
