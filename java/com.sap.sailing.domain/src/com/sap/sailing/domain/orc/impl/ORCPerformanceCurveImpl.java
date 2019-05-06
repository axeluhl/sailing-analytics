package com.sap.sailing.domain.orc.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.ranking.ORCPerformanceCurveRankingMetric;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * For a {@link Competitor} describes at which wind speeds and angles to the wind the boat is assumed to go at which
 * speed. This represents a simplified polar sheet with "knots" providing values for specific, discrete true wind angles
 * and true wind speeds, with bounds for "optimum beat angle" and "optimum run angle." See also <a
 * href="http://bugzilla.sapsailing.com/bugzilla/attachment.cgi?id=146">http://bugzilla.sapsailing.com/bugzilla/attachment.cgi?id=146</a>
 * for details. The true wind angles are symmetrical, assuming that the boat performs equally well on both tacks.
 * 
 * @author Daniel Lisunkin (i505543)
 * 
 */
public class ORCPerformanceCurveImpl implements Serializable, ORCPerformanceCurve {
    private static final long serialVersionUID = 4113356173492168453L;

    private final Map<Speed, Map<Bearing, Duration>> durationPerNauticalMileAtTrueWindAngleAndSpeed;
    
    /**
     * The beat angles for the true wind speeds; key set is equal to that of {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}
     */
    private final Map<Speed, Bearing> beatAngles;
    
    /**
     * The gybe angles for the true wind speeds; key set is equal to that of {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}
     */
    private final Map<Speed, Bearing> gybeAngles;
    
    /**
     * These Lagrange polynomials approximate the function mapping the true wind angle to the time allowance for one
     * nautical mile at the true wind speed given by the key. The key set equals that of
     * {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}.
     */
    private transient Map<Speed, PolynomialFunctionLagrangeForm> lagrangePolynomialsPerTrueWindSpeed;
    
    /**
     * Accepts the simplified polar data, one "column" for each of the defined true wind speeds, where each
     * column is a map from the true wind angle (here expressed as an object of type {@link Bearing}) and
     * the {@link Duration} the boat is assumed to need at that true wind speed/angle for one nautical mile.
     */
    public ORCPerformanceCurveImpl(Map<Speed, Map<Bearing, Duration>> twaAllowances, Map<Speed, Bearing> beatAngles, Map<Speed, Bearing> gybeAngles) {
        Map<Speed, Map<Bearing, Duration>> map = twaAllowances;
        durationPerNauticalMileAtTrueWindAngleAndSpeed = Collections.unmodifiableMap(map);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.gybeAngles = Collections.unmodifiableMap(gybeAngles);
        lagrangePolynomialsPerTrueWindSpeed = createLagrangePolynomials();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        lagrangePolynomialsPerTrueWindSpeed = createLagrangePolynomials();
    }

    private Map<Speed, PolynomialFunctionLagrangeForm> createLagrangePolynomials() {
        Map<Speed, PolynomialFunctionLagrangeForm> writeableLagrange = new HashMap<>();
        for (Entry<Speed, Map<Bearing, Duration>> twsAndTwaToDuration : durationPerNauticalMileAtTrueWindAngleAndSpeed.entrySet()) {
            final int numberOfTrueWindAngles = twsAndTwaToDuration.getValue().size();
            double[] twaInDegrees = new double[numberOfTrueWindAngles];
            double[] durationInSeconds = new double[numberOfTrueWindAngles];
            int i=0;
            for (Entry<Bearing, Duration> twaAndDuration : twsAndTwaToDuration.getValue().entrySet()) {
                twaInDegrees[i] = twaAndDuration.getKey().getDegrees();
                durationInSeconds[i] = twaAndDuration.getValue().asSeconds();
                i++;
            }
            writeableLagrange.put(twsAndTwaToDuration.getKey(),
                    new PolynomialFunctionLagrangeForm(twaInDegrees, durationInSeconds));
        }
        return Collections.unmodifiableMap(writeableLagrange);
    }
    
    /**
     * Computes the time allowance (expected duration) that the boat described by this certificate will get for the
     * <code>leg</code>. The true wind direction expected for the leg can optionally be provided using the
     * <code>trueWindToDirection</code> parameter. If that parameter is left <code>null</code>, a wind average will be
     * taken for the leg, based on the times when competitors entered and finished the leg.
     * 
     * @param trueWindToDirection
     *            if <code>null</code>, the true wind direction is taken from the leg's
     *            {@link TrackedLeg#getTrackedRace() tracked race} (see {@link TrackedLeg#getAverageTrueWindDirection()}).
     * 
     * @return a map telling for each of the standard true wind speeds which as also key in
     *         {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed} which duration is expected for the competitor
     *         described by this certificate to sail the complete <code>leg</code>.
     */
    protected Map<Speed, Duration> getAllowancesForLegPerTrueWindSpeed(TrackedLeg leg, Bearing trueWindToDirection) throws FunctionEvaluationException {
        Bearing legBearing = leg.getLegBearing(leg.getReferenceTimePoint());
        Distance greatCircleLegDistance = leg.getGreatCircleDistance(leg.getReferenceTimePoint());
        if (trueWindToDirection == null) {
            trueWindToDirection = leg.getAverageTrueWindDirection().getObject().getBearing();
        }
        return getAllowancesForLegPerTrueWindSpeed(trueWindToDirection, legBearing, greatCircleLegDistance);
    }
    
    /**
     * Calculates the times that the competitor is expected to have taken to reach her position at
     * <code>timePoint</code> for the different true wind speeds. This can be used to calculate the implied wind, given
     * the actual duration the competitor took, by constructing a spline from the knots defined by the resulting map,
     * mapping durations to implied wind speeds.
     * <p>
     * 
     * If the <code>competitor</code> hasn't started the race yet, <code>null</code> is returned.
     */
    protected Map<Speed, Duration> getAllowances(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        final Map<Speed, Duration> result;
        if (trackedRace.getMarkPassings(competitor).isEmpty()) {
            result = null;
        } else {
            result = new HashMap<>();
            Course course = trackedRace.getRace().getCourse();
            course.lockForRead();
            try {
                for (Leg leg : course.getLegs()) {
                    TrackedLegOfCompetitor tloc = trackedRace.getTrackedLeg(competitor, leg);
                    if (!tloc.hasStartedLeg(timePoint)) {
                        break; // consider only legs that the competitor has at least started to sail
                    } else {
                        if (tloc.hasFinishedLeg(timePoint)) {
                            // entire leg sailed
                            // TODO continue here...
                        }
                    }
                }
            } finally {
                course.unlockAfterRead();
            }
        }
        return result;
    }
    
    // TODO keep in mind when projecting a boat onto the leg direction that TrackedLeg et al. won't know about the beat/gybe angles here.
    // Therefore, it may be a bit tricky to determine the "leg" distance for a competitor sailing anywhere on the course. Projecting
    // onto the wind based on a guess what the beat angle may be can lead to incorrect results for angles that the guessed beat angle
    // may already call an upwind beat where this polar still considers it a reach...

    /**
     * Same as {@link #getAllowancesForLegPerTrueWindSpeed(TrackedLeg, Bearing)}, but here any leg direction and great circle
     * leg distance can be provided. This can be useful to obtain allowances for parts of a leg only.
     */
    private Map<Speed, Duration> getAllowancesForLegPerTrueWindSpeed(Bearing trueWindToDirection, Bearing legBearing,
            Distance greatCircleLegDistance) throws FunctionEvaluationException {
        Bearing trueWindAngle = legBearing.getDifferenceTo(trueWindToDirection.reverse());
        final double twaInDegrees = trueWindAngle.getDegrees();
        Map<Speed, Duration> result = new HashMap<>();
        for (Entry<Speed, PolynomialFunctionLagrangeForm> polyForSpeed : lagrangePolynomialsPerTrueWindSpeed.entrySet()) {
            final MillisecondsDurationImpl durationPerNauticalMileAtTwa = new MillisecondsDurationImpl(
                                                    (long) (1000. * polyForSpeed.getValue().value(twaInDegrees)));
            final Distance legDistance_ProjectedForBeatAndRun_GreatCircleForAllOthers;
            if (twaInDegrees < beatAngles.get(polyForSpeed.getKey()).getDegrees() || twaInDegrees > gybeAngles.get(polyForSpeed.getKey()).getDegrees()) {
                // project leg's distance to the wind
                legDistance_ProjectedForBeatAndRun_GreatCircleForAllOthers = greatCircleLegDistance.scale(Math.cos(trueWindAngle.getRadians()));
            } else {
                legDistance_ProjectedForBeatAndRun_GreatCircleForAllOthers = greatCircleLegDistance;
            }
            result.put(polyForSpeed.getKey(), durationPerNauticalMileAtTwa
                    .times(legDistance_ProjectedForBeatAndRun_GreatCircleForAllOthers.getNauticalMiles()));
        }
        return result;
    }
    
    PolynomialFunctionLagrangeForm getLagrangeInterpolationPerTrueWindSpeed(Bearing trueWindDirection) {
        return lagrangePolynomialsPerTrueWindSpeed.getOrDefault(trueWindDirection, null);
    }

    @Override
    public Speed getImpliedWind() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getCalculatedTime() {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
