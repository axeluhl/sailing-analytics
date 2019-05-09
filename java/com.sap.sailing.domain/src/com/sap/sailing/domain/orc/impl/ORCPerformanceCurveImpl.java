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
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.orc.ORCPerformanceCurveLeg;
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
    
    public static final Speed[] ALLOWANCES_SPEED_DELTAS = { new KnotSpeedImpl( 6),
                                                            new KnotSpeedImpl( 8),
                                                            new KnotSpeedImpl(10),
                                                            new KnotSpeedImpl(12),
                                                            new KnotSpeedImpl(14),
                                                            new KnotSpeedImpl(16),
                                                            new KnotSpeedImpl(20)};

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
    
    PolynomialFunctionLagrangeForm getLagrangeInterpolationPerTrueWindSpeed(Bearing trueWindDirection) {
        return lagrangePolynomialsPerTrueWindSpeed.getOrDefault(trueWindDirection, null);
    }

    Map<Speed, Duration> createAllowancesPerCourse(ORCPerformanceCurveCourse course) throws FunctionEvaluationException {
        Map<Speed, Duration> result = new HashMap<>();
        Map<ORCPerformanceCurveLeg, Map<Speed, Duration>> allowancesPerLeg = new HashMap<>();
        
        for (ORCPerformanceCurveLeg leg : course.getLegs()) {
            allowancesPerLeg.put(leg, createAllowancePerLeg(leg));
        }

        for (Speed tws : ALLOWANCES_SPEED_DELTAS) {
            Duration allowancePerTws = new MillisecondsDurationImpl(0);
            
            //for ()
        }
        
        return result;
    }
    
    Map<Speed, Duration> createAllowancePerLeg(ORCPerformanceCurveLeg leg) throws FunctionEvaluationException {
        Map<Speed, Duration> result = new HashMap<>();
        Double twa = leg.getTwa().getDegrees(); 
        
        for (Entry<Speed, PolynomialFunctionLagrangeForm> entry : lagrangePolynomialsPerTrueWindSpeed.entrySet()) {
          //TODO Case switching on TWA (0. TWA = 0; 1. TWA < Beat; 2. Beat < TWA < Gybe; 3. Gybe < TWA)
            if (twa < beatAngles.get(entry.getKey()).getDegrees()) {
                // Case 0&1
                //result.put(entry.getKey(), Duration.ONE_SECOND.times(beatAngles.get(entry.getKey()) * leg.getLength().getNauticalMiles()));
                // Need Beat Angle Allowances for different TWS, maybe Constructor and Structure need to be changed, to get direct access? What will be nicer?
                // result will be: beatVMG * distance / cos(TWA)
            }
            else if (twa > gybeAngles.get(entry.getKey()).getDegrees()) {
                // Case 3
                // result will be: runVMG * distance / cos(TWA)
            }
            else {
                // Case 2
                result.put(entry.getKey(), Duration.ONE_SECOND.times(entry.getValue().value(twa) * leg.getLength().getNauticalMiles()));
            }
        }
        
        return result;
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
