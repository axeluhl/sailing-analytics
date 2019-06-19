package com.sap.sailing.domain.orc.impl;

import java.io.Serializable;
import java.util.Arrays;
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
 * For a {@link Competitor} and the {@link ORCPerformanceCurveCourse} which the competitor sailed until the creation of
 * an instance, this class represents a so called "Perfromance Curve". This Curve is specified by the so called "Implied
 * Wind" a {@link Speed} on the x-Axis and the allowance in s/nm respectively a {@link Duration} on the y-Axis. It
 * represents a simplified polar curve for the given boat and the given part of the course. For a given windspeed the
 * performance curve returns the allowance for the boat or in simpler words: how long should the boat need for a
 * nautical mile when sailing 100% performance.
 * 
 * The implementation is oriented to the pascal code provided by ORC for the performance curve module. Available here
 * <a href="https://data.orc.org/tools.php?c=pcs">https://data.orc.org/tools.php?c=pcs</a>.
 * 
 * See also <a href=
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
    private final PolynomialSplineFunction functionImpliedWindToAllowance;

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
        durationPerNauticalMileAtTrueWindAngleAndSpeed = Collections.unmodifiableMap(twaAllowances);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.gybeAngles = Collections.unmodifiableMap(gybeAngles);
        this.course = course;
        functionImpliedWindToAllowance = createPerformanceCurve();
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
        
        
        for (Speed entry : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            // Case switching on TWA (0. TWA == 0; 1. TWA < Beat; 2. Beat < TWA < Gybe; 3. Gybe < TWA; 4. TWA == 180)
            if (twa < beatAngles.get(entry).getDegrees()) {
                // Case 0 & 1 - result = beatVMG * distance / cos(TWA)
                result.put(entry, Duration.ONE_SECOND.times(getDurationPerNauticalMileAtTrueWindAngleAndSpeed(entry, beatAngles.get(entry)).asSeconds() * leg.getLength().getNauticalMiles() * Math.abs(Math.cos(Math.toRadians(twa)))));
            } else if (twa > gybeAngles.get(entry).getDegrees()) {
                // Case 3 & 4 - result = runVMG * distance / cos(TWA)
                result.put(entry, Duration.ONE_SECOND.times(getDurationPerNauticalMileAtTrueWindAngleAndSpeed(entry, gybeAngles.get(entry)).asSeconds() * leg.getLength().getNauticalMiles() * Math.abs(Math.cos(Math.toRadians(twa)))));
            } else {
                // Case 2 - result is given through the laGrange Interpolation, between the Beat and Gybe Angles
                result.put(entry, getLagrangeAllowancePerTrueWindSpeedAndAngle(entry, leg.getTwa()).times(leg.getLength().getNauticalMiles()));
            }
        }
        
        return result;
    }

    private PolynomialSplineFunction createPerformanceCurve() {
        Map<Speed, Duration> allowances;
        try {
            allowances = createAllowancesPerCourse();
        } catch (FunctionEvaluationException e) {
            e.printStackTrace();
            return null;
        }
        SplineInterpolator interpolator = new SplineInterpolator();
        double[] xs = new double[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length];
        double[] ys = new double[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length];
        int i = 0;
        
        for(Entry<Speed,Duration> entry : allowances.entrySet()) {
            xs[i] = entry.getKey().getKnots();
            ys[i] = entry.getValue().asSeconds();
            i += 1;
        }
        
        ArrayUtils.reverse(xs);
        ArrayUtils.reverse(ys);
        return interpolator.interpolate(xs, ys);
    }
    
    @Override
    public Speed getImpliedWind(Duration time) throws MaxIterationsExceededException, FunctionEvaluationException{
        PolynomialFunction workingFunction;
        double[] allowancesInSeconds = new double[functionImpliedWindToAllowance.getKnots().length];
        
        // TODO Corner cases for Allowance > Allowance(20kt) or Allowance < Allowance(6kt)
        
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
        // TODO Corner cases for ImpliedWind > 20kt or ImpliedWind < 6kt
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
    public Duration getCalculatedTime(ORCPerformanceCurve referenceBoat, Duration sailedDurationPerNauticalMile) {
        try {
            return referenceBoat.getAllowancePerCourse(getImpliedWind(sailedDurationPerNauticalMile));
        } catch (MaxIterationsExceededException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FunctionEvaluationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public Duration getLagrangeAllowancePerTrueWindSpeedAndAngle(Speed trueWindSpeed, Bearing trueWindAngle) throws FunctionEvaluationException, IllegalArgumentException {
        Duration result;
        Bearing[] allowancesTrueWindAnglesWithBeatRun = (Bearing[]) ArrayUtils.addAll(new Bearing[] {beatAngles.get(trueWindSpeed)}, ArrayUtils.addAll(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES, new Bearing[] {gybeAngles.get(trueWindSpeed)}));
        Arrays.sort(allowancesTrueWindAnglesWithBeatRun);
        
        // Controlling whether the gybe angle is greater then the other polars and eliminating those from the array
        int frontcutpoint = Arrays.binarySearch(allowancesTrueWindAnglesWithBeatRun, beatAngles.get(trueWindSpeed));
        int backcutpoint  = Arrays.binarySearch(allowancesTrueWindAnglesWithBeatRun, gybeAngles.get(trueWindSpeed)) + 1;
        allowancesTrueWindAnglesWithBeatRun = Arrays.copyOfRange(allowancesTrueWindAnglesWithBeatRun, frontcutpoint, backcutpoint);
        
        int i = -1 ; // after the loop, i equals the next higher available polar data for the given TWA
        for(int j = 0; j < allowancesTrueWindAnglesWithBeatRun.length; j++) {
            if(trueWindAngle.compareTo(allowancesTrueWindAnglesWithBeatRun[j]) < 0) {
                i = j;
                break;
            }
            if(trueWindAngle.compareTo(allowancesTrueWindAnglesWithBeatRun[j]) == 0 && j == allowancesTrueWindAnglesWithBeatRun.length - 1) {
                // TODO Bug here: legTWA == 180° and RunAngle == 180° => special case not handled
                i = j;
                break;
            }
        }
        
        // This part is implemented equally to the part from the ORC PCSLib.pas.
        // ORC decides to use only up to the nearest 4 values to the searched TWA for interpolation.
        if (i >= 0) {
            int upperBound = Math.min(i + 1, allowancesTrueWindAnglesWithBeatRun.length - 1);
            int lowerBound = Math.max(i - 2, 0);
            double[] xn = new double[upperBound - lowerBound + 1];
            double[] yn = new double[upperBound - lowerBound + 1];
            for (i = lowerBound; i <= upperBound; i++) {
                xn[i-lowerBound] = allowancesTrueWindAnglesWithBeatRun[i].getDegrees();
                yn[i-lowerBound] = durationPerNauticalMileAtTrueWindAngleAndSpeed.get(trueWindSpeed).get(allowancesTrueWindAnglesWithBeatRun[i]).asSeconds();
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

    @Override
    public String toString() {
        try {
            return "ORCPerformanceCurve [Allowances " + createAllowancesPerCourse() + "]";
        } catch (FunctionEvaluationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    
}
