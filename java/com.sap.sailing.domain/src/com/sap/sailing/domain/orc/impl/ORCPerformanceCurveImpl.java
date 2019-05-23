package com.sap.sailing.domain.orc.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.orc.ORCPerformanceCurveLeg;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

/**
 * For a {@link Competitor} describes at which wind speeds and angles to the wind the boat is assumed to go at which
 * speed. This represents a simplified polar sheet with "knots" providing values for specific, discrete true wind angles
 * and true wind speeds, with bounds for "optimum beat angle" and "optimum run angle." See also <a href=
 * "http://bugzilla.sapsailing.com/bugzilla/attachment.cgi?id=146">http://bugzilla.sapsailing.com/bugzilla/attachment.cgi?id=146</a>
 * for details. The true wind angles are symmetrical, assuming that the boat performs equally well on both tacks.
 * 
 * @author Daniel Lisunkin (i505543)
 * 
 */
public class ORCPerformanceCurveImpl implements Serializable, ORCPerformanceCurve {
    private static final long serialVersionUID = 4113356173492168453L;

    //TODO COMMENT for key
    private final Map<Speed, Map<Bearing, Duration>> durationPerNauticalMileAtTrueWindAngleAndSpeed;

    /**
     * The beat angles for the true wind speeds; key set is equal to that of
     * {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}
     */
    private final Map<Speed, Bearing> beatAngles;

    /**
     * The gybe angles for the true wind speeds; key set is equal to that of
     * {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}
     */
    private final Map<Speed, Bearing> gybeAngles;

    /**
     * These Lagrange polynomials approximate the function mapping the true wind angle to the time allowance for one
     * nautical mile at the true wind speed given by the key. The key set equals that of
     * {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}.
     */
    private transient Map<Speed, PolynomialFunctionLagrangeForm> lagrangePolynomialsPerTrueWindSpeed;
    
    /*
     * 
     */
    private final ORCPerformanceCurveCourse course;

    /**
     * Accepts the simplified polar data, one "column" for each of the defined true wind speeds, where each column is a
     * map from the true wind angle (here expressed as an object of type {@link Bearing}) and the {@link Duration} the
     * boat is assumed to need at that true wind speed/angle for one nautical mile.
     */
    public ORCPerformanceCurveImpl(Map<Speed, Map<Bearing, Duration>> twaAllowances, Map<Speed, Bearing> beatAngles,
            Map<Speed, Bearing> gybeAngles, ORCPerformanceCurveCourse course) {
        durationPerNauticalMileAtTrueWindAngleAndSpeed = Collections.unmodifiableMap(twaAllowances);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.gybeAngles = Collections.unmodifiableMap(gybeAngles);
        this.course = course;
        lagrangePolynomialsPerTrueWindSpeed = createLagrangePolynomials();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        lagrangePolynomialsPerTrueWindSpeed = createLagrangePolynomials();
    }

    private Map<Speed, PolynomialFunctionLagrangeForm> createLagrangePolynomials() {
        Map<Speed, PolynomialFunctionLagrangeForm> writeableLagrange = new HashMap<>();
        for (Entry<Speed, Map<Bearing, Duration>> twsAndTwaToDuration : durationPerNauticalMileAtTrueWindAngleAndSpeed
                .entrySet()) {
            final int numberOfTrueWindAngles = twsAndTwaToDuration.getValue().size();
            double[] twaInDegrees = new double[numberOfTrueWindAngles];
            double[] durationInSeconds = new double[numberOfTrueWindAngles];
            int i = 0;
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

    public Map<Speed, Duration> createAllowancesPerCourse() throws FunctionEvaluationException {
        Map<Speed, Duration> result = new HashMap<>();
        Map<ORCPerformanceCurveLeg, Map<Speed, Duration>> allowancesPerLeg = new HashMap<>();
        
        for (ORCPerformanceCurveLeg leg : course.getLegs()) {
            allowancesPerLeg.put(leg, createAllowancePerLeg(leg));
        }

        for (Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            Double allowancePerTws = 0.0;
            
            for (Entry<ORCPerformanceCurveLeg, Map<Speed, Duration>> entry : allowancesPerLeg.entrySet()) {
                allowancePerTws += Math.abs(entry.getValue().get(tws).asSeconds());
            }
            
            result.put(tws, Duration.ONE_SECOND.times(allowancePerTws));
        }
        
        for (Entry<Speed, Duration> entry : result.entrySet()) {
            result.replace(entry.getKey(), entry.getValue().divide(course.getTotalLength().getNauticalMiles()));
        }
        
        return result;
    }

    private Map<Speed, Duration> createAllowancePerLeg(ORCPerformanceCurveLeg leg) throws FunctionEvaluationException {
        Map<Speed, Duration> result = new HashMap<>();
        Double twa = leg.getTwa().getDegrees();

        for (Entry<Speed, PolynomialFunctionLagrangeForm> entry : lagrangePolynomialsPerTrueWindSpeed.entrySet()) {
            // Case switching on TWA (0. TWA = 0; 1. TWA < Beat; 2. Beat < TWA < Gybe; 3. Gybe < TWA)
            if (twa < beatAngles.get(entry.getKey()).getDegrees()) {
                // Case 0&1
                // Need Beat Angle Allowances for different TWS, maybe Constructor and Structure need to be changed, to
                // get direct access? What will be nicer?
                // result will be: beatVMG * distance / cos(TWA)
                result.put(entry.getKey(),
                        Duration.ONE_SECOND.times(entry.getValue().value(beatAngles.get(entry.getKey()).getDegrees())
                                * leg.getLength().getNauticalMiles() * Math.cos(Math.toRadians(twa))));
            } else if (twa > gybeAngles.get(entry.getKey()).getDegrees()) {
                // Case 3
                // result will be: runVMG * distance / cos(TWA)
                result.put(entry.getKey(),
                        Duration.ONE_SECOND.times(entry.getValue().value(gybeAngles.get(entry.getKey()).getDegrees())
                                * leg.getLength().getNauticalMiles() * Math.cos(Math.toRadians(twa))));
            } else {
                // Case 2
                // result is given through the laGrange Interpolation, between the Beat and Gybe Angles
                result.put(entry.getKey(),
                        Duration.ONE_SECOND.times(entry.getValue().value(twa) * leg.getLength().getNauticalMiles()));
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
    public Duration getCalculatedTime(ORCPerformanceCurve referenceBoat) {
        // TODO Auto-generated method stub
        return null;
    }
    
    // LAGRANGE TEST BASE because of unusual implementation in ORC Pascal code
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public Duration getLagrangeInterpolationPerTrueWindSpeedAndAngle(Speed trueWindSpeed, Bearing trueWindAngle) throws FunctionEvaluationException, IllegalArgumentException {
        Bearing[] allowancesTrueWindAnglesWithBeatRun = (Bearing[]) ArrayUtils.addAll(new Bearing[] {beatAngles.get(trueWindSpeed)}, ArrayUtils.addAll(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES, new Bearing[] {gybeAngles.get(trueWindSpeed)}));
        
        Bearing[] ALLOWANCES_TRUE_WIND_ANGLES = allowancesTrueWindAnglesWithBeatRun;
        Arrays.sort(ALLOWANCES_TRUE_WIND_ANGLES);
        
        int j = -1;
        for(int i = 0; i < ALLOWANCES_TRUE_WIND_ANGLES.length; i++) {
            if(trueWindAngle.compareTo(ALLOWANCES_TRUE_WIND_ANGLES[i]) < 0) {
                j = i;
                break;
            }
        }
        
        if (j < 0) {
            return null;
        }
        
        int ne = Math.min(j + 1, ALLOWANCES_TRUE_WIND_ANGLES.length - 1);
        int ns = Math.max(j - 2, 0);
        
        double[] xn = new double[ne - ns + 1];
        double[] yn = new double[ne - ns + 1];
        
        for (j = ns; j <= ne; j++) {
            xn[j-ns] = ALLOWANCES_TRUE_WIND_ANGLES[j].getDegrees();
            yn[j-ns] = durationPerNauticalMileAtTrueWindAngleAndSpeed.get(trueWindSpeed).get(ALLOWANCES_TRUE_WIND_ANGLES[j]).asSeconds();
        }
            
        return Duration.ONE_SECOND.times(new PolynomialFunctionLagrangeForm(xn, yn).value(trueWindAngle.getDegrees()));
    }
    
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public PolynomialFunctionLagrangeForm getLagrangeInterpolationPerTrueWindSpeed(Speed trueWindSpeed) {
        return lagrangePolynomialsPerTrueWindSpeed.getOrDefault(trueWindSpeed, null);
    }
    
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public Duration getDurationPerNauticalMileAtTrueWindAngleAndSpeed(Speed trueWindSpeed, Bearing trueWindAngle) {
        return durationPerNauticalMileAtTrueWindAngleAndSpeed.getOrDefault(trueWindSpeed, null).getOrDefault(trueWindAngle, null);
    }

}
