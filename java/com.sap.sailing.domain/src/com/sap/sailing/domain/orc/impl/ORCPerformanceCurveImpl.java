package com.sap.sailing.domain.orc.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math.analysis.solvers.NewtonSolver;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
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

    /**
     * Same two dimensional map as in the corresponding ORCCertificate. The result is a {@link Duration} called
     * allowance for a boat at a given TWS and TWA. The first key set contains {@link Speed}s and is equal to the
     * windspeeds defined in the static field of {@link ORCCertificateImpl}. The second key set contains
     * {@link Bearing}s and is equal to the windangles defined in the static fields of the same class.
     * Needed for LaGrange interpolation.
     */
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
    
    /**
     * The specific course for which the PerformanceCurve of a boat is calculated. The course is set during the
     * constructor call.
     */
    private final ORCPerformanceCurveCourse course;
    
    /**
     * This PolynomialSplineFunction is created with the array of course specific allowances for the boat which this
     * ORCPerformanceCurve belongs to. This function contains subfunctions for each interval between two given
     * calculated points. The input for the function is the value of the implied wind (speed in kts) and the output an
     * allowance in sec/nm.
     */
    private PolynomialSplineFunction functionImpliedWindToAllowance;
    
    //TODO delete, after clarification with ORC. Hopefully not needed anymore.
    private PolynomialSplineFunction functionAllowanceToTwa;

    /**
     * Accepts the simplified polar data, one "column" for each of the defined true wind speeds, where each column is a
     * map from the true wind angle (here expressed as an object of type {@link Bearing}) and the {@link Duration} the
     * boat is assumed to need at that true wind speed/angle for one nautical mile.
     *     
     * @param twaAllowances
     * @param beatAngles
     * @param gybeAngles
     * @param course
     */
    public ORCPerformanceCurveImpl(Map<Speed, Map<Bearing, Duration>> twaAllowances, Map<Speed, Bearing> beatAngles,
            Map<Speed, Bearing> gybeAngles, ORCPerformanceCurveCourse course) {
        //TODO Update constructor with duration to the current progress of the boat on the race course
        durationPerNauticalMileAtTrueWindAngleAndSpeed = Collections.unmodifiableMap(twaAllowances);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.gybeAngles = Collections.unmodifiableMap(gybeAngles);
        this.course = course;
        // TODO extract following statement and functionality to the ORCCertificate interface and implementations.
        lagrangePolynomialsPerTrueWindSpeed = createLagrangePolynomials();
        try {
            initializePerformanceCurve();
        } catch (FunctionEvaluationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        lagrangePolynomialsPerTrueWindSpeed = createLagrangePolynomials();
    }

    //TODO Decide if needed or if it can be removed. Depending on the information provided by ORC.
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

    /**
     * TODO Comment
     * @return
     * @throws FunctionEvaluationException
     */
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
            // Case switching on TWA (0. TWA == 0; 1. TWA < Beat; 2. Beat < TWA < Gybe; 3. Gybe < TWA; 4. TWA == 180)
            if (twa < beatAngles.get(entry.getKey()).getDegrees()) {
                // Case 0&1
                // result will be: beatVMG * distance / cos(TWA)
                result.put(entry.getKey(),
                        Duration.ONE_SECOND.times(entry.getValue().value(beatAngles.get(entry.getKey()).getDegrees())
                                * leg.getLength().getNauticalMiles() * Math.cos(Math.toRadians(twa))));
            } else if (twa > gybeAngles.get(entry.getKey()).getDegrees()) {
                // Case 3 & 4
                // result will be: runVMG * distance / cos(TWA)
                result.put(entry.getKey(),
                        Duration.ONE_SECOND.times(entry.getValue().value(gybeAngles.get(entry.getKey()).getDegrees())
                                * leg.getLength().getNauticalMiles() * Math.cos(Math.toRadians(twa))));
            } else {
                // Case 2
                // result is given through the laGrange Interpolation, between the Beat and Gybe Angles
                result.put(entry.getKey(),
                        getLagrangeAllowancePerTrueWindSpeedAndAngle(entry.getKey(), leg.getTwa()).times(leg.getLength().getNauticalMiles()));
            }
        }

        return result;
    }

    private void initializePerformanceCurve() throws FunctionEvaluationException {
        Map<Speed, Duration> allowances = createAllowancesPerCourse();
        SplineInterpolator interpolator = new SplineInterpolator();
        double[] xs = new double[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length];
        double[] ys = new double[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length];
        int i = 0;
        
        for(Entry<Speed,Duration> entry : allowances.entrySet()) {
            xs[i] = entry.getKey().getKnots();
            ys[i] = entry.getValue().asSeconds();
            i += 1;
        }
        
        functionAllowanceToTwa = interpolator.interpolate(ys, xs); //Inverse Function of the "real" PerformanceCurve (ImpliedWind -> Allowance), not needed later
        ArrayUtils.reverse(xs);
        ArrayUtils.reverse(ys);
        functionImpliedWindToAllowance = interpolator.interpolate(xs, ys);
    }
    
    public Speed getImpliedWind(Duration time) throws ArgumentOutsideDomainException {
        return new KnotSpeedImpl(functionAllowanceToTwa.value(time.asSeconds()));
    }
    
    public Speed getImpliedWindNewton(Duration time) throws MaxIterationsExceededException, FunctionEvaluationException {
        PolynomialFunction workingFunction;
        double[] allowancesInSeconds = new double[functionImpliedWindToAllowance.getKnots().length];
        
        for (int i = 0; i < allowancesInSeconds.length; i++) {
            allowancesInSeconds[i] = functionImpliedWindToAllowance.value(functionImpliedWindToAllowance.getKnots()[i]);
        }
        
        int i = 0;
        while(i < allowancesInSeconds.length && time.asSeconds() <= allowancesInSeconds[i]) {
            i += 1;
        }
        i -= 1;
        workingFunction = functionImpliedWindToAllowance.getPolynomials()[i];
        
        //PolynomialFunction which will be solved by the Newton Approach
        PolynomialFunction subtractedFunction = workingFunction.subtract(new PolynomialFunction(new double[] {time.asSeconds()}));
        NewtonSolver solver = new NewtonSolver();
        solver.setAbsoluteAccuracy(0.000001);
        //TODO Comment for the special treatment of the solver
        return new KnotSpeedImpl(solver.solve(subtractedFunction, 0,
                functionImpliedWindToAllowance.getKnots()[i + 1] - functionImpliedWindToAllowance.getKnots()[i])
                + functionImpliedWindToAllowance.getKnots()[i]);
    }
    
    @Override
    public Duration getAllowancePerCourse(Speed impliedWind) {
        try {
            return Duration.ONE_SECOND.times(functionImpliedWindToAllowance.value(impliedWind.getKnots()));
        } catch (ArgumentOutsideDomainException e) {
            // TODO Auto-generated catch block
            // TODO Create senseful Exception Handling for this class, Logging ...
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Speed getImpliedWind() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getCalculatedTime(ORCPerformanceCurve referenceBoat) {
        return referenceBoat.getAllowancePerCourse(getImpliedWind());
    }
    
    // LAGRANGE TEST BASE because of unusual implementation in ORC Pascal code
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public Duration getLagrangeAllowancePerTrueWindSpeedAndAngle(Speed trueWindSpeed, Bearing trueWindAngle) throws FunctionEvaluationException, IllegalArgumentException {
        Duration result;
        Bearing[] allowancesTrueWindAnglesWithBeatRun = (Bearing[]) ArrayUtils.addAll(new Bearing[] {beatAngles.get(trueWindSpeed)}, ArrayUtils.addAll(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES, new Bearing[] {gybeAngles.get(trueWindSpeed)}));
        Bearing[] ALLOWANCES_TRUE_WIND_ANGLES = ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES; //allowancesTrueWindAnglesWithBeatRun; //TODO Cleanup, after more information from ORC is available
        //Arrays.sort(ALLOWANCES_TRUE_WIND_ANGLES);
        
        int i = -1; //after the loop, i equals the next higher available polar data for the given TWA
        for(int j = 0; j < ALLOWANCES_TRUE_WIND_ANGLES.length; j++) {
            if(trueWindAngle.compareTo(ALLOWANCES_TRUE_WIND_ANGLES[j]) < 0) {
                i = j;
                break;
            }
        }
        
        // This part is implemented equally to the part from the ORC PCSLib.pas.
        // ORC decides to use only the nearest 4 values to the searched TWA for interpolation.
        if (i >= 0) {
            int upperBound = Math.min(i + 1, ALLOWANCES_TRUE_WIND_ANGLES.length - 1);
            int lowerBound = Math.max(i - 2, 0);
            double[] xn = new double[upperBound - lowerBound + 1];
            double[] yn = new double[upperBound - lowerBound + 1];
            for (i = lowerBound; i <= upperBound; i++) {
                xn[i-lowerBound] = ALLOWANCES_TRUE_WIND_ANGLES[i].getDegrees();
                yn[i-lowerBound] = durationPerNauticalMileAtTrueWindAngleAndSpeed.get(trueWindSpeed).get(ALLOWANCES_TRUE_WIND_ANGLES[i]).asSeconds();
            }
            result = Duration.ONE_SECOND.times(new PolynomialFunctionLagrangeForm(xn, yn).value(trueWindAngle.getDegrees()));
        } else {
            result = null;
        }
        
        return result;
    }
    
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public Duration getDurationPerNauticalMileAtTrueWindAngleAndSpeed(Speed trueWindSpeed, Bearing trueWindAngle) {
        return durationPerNauticalMileAtTrueWindAngleAndSpeed.getOrDefault(trueWindSpeed, null).getOrDefault(trueWindAngle, null);
    }

}
