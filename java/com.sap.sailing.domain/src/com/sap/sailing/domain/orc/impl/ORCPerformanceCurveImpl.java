package com.sap.sailing.domain.orc.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
import com.sap.sse.common.impl.DegreeBearingImpl;

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
    private static final Logger logger = Logger.getLogger(ORCPerformanceCurveImpl.class.getName()); // TODO Daniel wants to add some logging!
    private static final long serialVersionUID = 4113356173492168453L;

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
    private final PolynomialSplineFunction functionImpliedWindInKnotsToAllowanceInSecondsForCourse;

    /**
     * Accepts the simplified polar data, one "column" for each of the defined true wind speeds, where each column is a
     * map from the true wind angle (here expressed as an object of type {@link Bearing}) and the {@link Duration} the
     * boat is assumed to need at that true wind speed/angle for one nautical mile.
     */
    public ORCPerformanceCurveImpl(Map<Speed, Map<Bearing, Speed>> twaAllowances, Map<Speed, Bearing> beatAngles,
            Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed, Map<Speed, Bearing> runAngles,
            Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed, ORCPerformanceCurveCourse course) throws FunctionEvaluationException {
        this.course = course;
        functionImpliedWindInKnotsToAllowanceInSecondsForCourse = createPerformanceCurve(twaAllowances, beatAngles,
                beatVMGPredictionPerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed);
    }

    /**
     * Computes the duration the boat to which this performance curve belongs is expected to sail to complete the
     * {@link #course}, keyed by the different true wind speeds. The resulting {@link LinkedHashMap}'s iteration
     * order is guaranteed to deliver the true wind speed keys in the order of ascending wind speeds.
     */
    public LinkedHashMap<Speed, Duration> createAllowancesPerCourse(Map<Speed, Map<Bearing, Speed>> twaAllowances,
            Map<Speed, Bearing> beatAngles, Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed,
            Map<Speed, Bearing> runAngles, Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed)
            throws FunctionEvaluationException {
        final LinkedHashMap<Speed, Duration> result = new LinkedHashMap<>();
        final Map<ORCPerformanceCurveLeg, Map<Speed, Duration>> allowancesPerLeg = new HashMap<>();
        for (final ORCPerformanceCurveLeg leg : course.getLegs()) {
            allowancesPerLeg.put(leg, createAllowancePerLeg(leg, twaAllowances, beatAngles,
                    beatVMGPredictionPerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed));
        }
        for (final Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            double allowancePerTws = 0.0;
            for (final Entry<ORCPerformanceCurveLeg, Map<Speed, Duration>> entry : allowancesPerLeg.entrySet()) {
                allowancePerTws += Math.abs(entry.getValue().get(tws).asSeconds());
            }
            result.put(tws, Duration.ONE_SECOND.times(allowancePerTws));
        }
        return result;
    }

    /**
     * Computes the durations the boat to which this performance curve belongs is expected to sail to complete the {@code leg},
     * considering the leg's {@link ORCPerformanceCurveLeg#getLength() length}.
     */
    private Map<Speed, Duration> createAllowancePerLeg(ORCPerformanceCurveLeg leg,
            Map<Speed, Map<Bearing, Speed>> twaAllowances, Map<Speed, Bearing> beatAngles,
            Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed, Map<Speed, Bearing> runAngles,
            Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed) throws FunctionEvaluationException {
        final Map<Speed, Duration> result = new HashMap<>();
        for (final Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            // Case switching on TWA (0. TWA == 0; 1. TWA < Beat; 2. Beat < TWA < Gybe; 3. Gybe < TWA; 4. TWA == 180)
            if (leg.getTwa().compareTo(beatAngles.get(tws)) <= 0) {
                // Case 0 & 1 - result = beatVMG * distance * cos(TWA)
                result.put(tws, beatVMGPredictionPerTrueWindSpeed.get(tws).getDuration(
                        /* rhumb line distance of upwind leg projected to wind */ leg.getLength().scale(Math.cos(leg.getTwa().getRadians()))));
            } else if (leg.getTwa().compareTo(runAngles.get(tws)) >= 0) {
                // Case 3 & 4 - result = runVMG * distance * cos(TWA)
                result.put(tws, runVMGPredictionPerTrueWindSpeed.get(tws).getDuration(
                        /* rhumb line distance of downwind leg projected to wind */ leg.getLength().scale(Math.cos(Math.PI-leg.getTwa().getRadians()))));
            } else {
                // Case 2 - result is given through the laGrange Interpolation, between the Beat and Gybe Angles
                result.put(tws,
                        getLagrangeSpeedPredictionForTrueWindSpeedAndAngle(twaAllowances, beatAngles,
                                beatVMGPredictionPerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed, tws,
                                leg.getTwa()).getDuration(leg.getLength()));
            }
        }
        return result;
    }

    /**
     * Computes a function that, given a true wind speed (TWS) calculates the duration (in seconds) that the boat to
     * which this performance curve belongs is expected to sail to complete the {@link #course}.
     */
    private PolynomialSplineFunction createPerformanceCurve(Map<Speed, Map<Bearing, Speed>> twaAllowances,
            Map<Speed, Bearing> beatAngles, Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed,
            Map<Speed, Bearing> runAngles, Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed) throws FunctionEvaluationException {
        final Map<Speed, Duration> allowancesForCoursePerTrueWindSpeed = createAllowancesPerCourse(twaAllowances, beatAngles,
                beatVMGPredictionPerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed);
        SplineInterpolator interpolator = new SplineInterpolator();
        double[] xs = new double[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length];
        double[] ys = new double[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length];
        int i = 0;
        for (final Entry<Speed, Duration> entry : allowancesForCoursePerTrueWindSpeed.entrySet()) {
            xs[i] = entry.getKey().getKnots();
            ys[i] = entry.getValue().asSeconds();
            i++;
        }
        return interpolator.interpolate(xs, ys);
    }
    
    @Override
    public Speed getImpliedWind(Duration durationToCompleteCourse) throws MaxIterationsExceededException, FunctionEvaluationException{
        PolynomialFunction workingFunction;
        double durationToCompleteCourseInSeconds = durationToCompleteCourse.asSeconds();
        double[] allowancesInSecondsToCompleteCourse = Arrays.stream(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS).mapToDouble(tws->{
            try {
                return functionImpliedWindInKnotsToAllowanceInSecondsForCourse.value(tws.getKnots());
            } catch (ArgumentOutsideDomainException e) {
                throw new RuntimeException(e);
            }
        }).toArray();
        final Speed result;
        // Corner cases for Allowance > Allowance(20kt) or Allowance < Allowance(6kt)
        if (durationToCompleteCourseInSeconds <= allowancesInSecondsToCompleteCourse[allowancesInSecondsToCompleteCourse.length - 1]) {
            result = ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS.length-1];
        } else if (durationToCompleteCourseInSeconds >= allowancesInSecondsToCompleteCourse[0]) {
            result = ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0];
        } else {
            // find the polynomial splined function that produces the durationToCompleteCourse within its validity range
            int i = 0;
            while (i < allowancesInSecondsToCompleteCourse.length && durationToCompleteCourseInSeconds <= allowancesInSecondsToCompleteCourse[i]) {
                i++;
            }
            i--;
            workingFunction = functionImpliedWindInKnotsToAllowanceInSecondsForCourse.getPolynomials()[i];
            //PolynomialFunction which will be solved by the Newton Approach
            PolynomialFunction subtractedFunction = workingFunction.subtract(new PolynomialFunction(new double[] {durationToCompleteCourseInSeconds}));
            NewtonSolver solver = new NewtonSolver();
            solver.setAbsoluteAccuracy(0.000001);
            // TODO Comment for the special treatment of the solver
            result = new KnotSpeedImpl(solver.solve(subtractedFunction, 0,
                    functionImpliedWindInKnotsToAllowanceInSecondsForCourse.getKnots()[i + 1] - functionImpliedWindInKnotsToAllowanceInSecondsForCourse.getKnots()[i])
                    + functionImpliedWindInKnotsToAllowanceInSecondsForCourse.getKnots()[i]);
        }
        return result;
    }
    
    @Override
    public Duration getAllowancePerCourse(Speed impliedWind) throws ArgumentOutsideDomainException {
        return Duration.ONE_SECOND.times(functionImpliedWindInKnotsToAllowanceInSecondsForCourse.value(impliedWind.getKnots()));
    }

    @Override
    public ORCPerformanceCurveCourse getCourse() {
        return course;
    }

    @Override
    public Duration getCalculatedTime(ORCPerformanceCurve referenceBoat, Duration sailedDurationPerNauticalMile)
            throws MaxIterationsExceededException, FunctionEvaluationException {
        return referenceBoat.getAllowancePerCourse(getImpliedWind(sailedDurationPerNauticalMile));
    }
    
    /**
     * @return an array containing two arrays: the first array holds the TWAs, the second array holds the corresponding
     *         speed over ground in knots for the TWA at the corresponding index in the first array
     */
    private double[][] createPolarsPerTrueWindSpeed(Speed trueWindSpeed, Map<Speed, Map<Bearing, Speed>> reachingSpeedPredictionsPerTrueWindSpeedAndAngle,
            Map<Speed, Bearing> beatAngles, Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed,
            Map<Speed, Bearing> runAngles, Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed) {
        ArrayList<Double> resultWindAngles = new ArrayList<>();
        ArrayList<Double> resultSpeedsOverGroundInKnots = new ArrayList<>();
        Bearing beatAngle = beatAngles.get(trueWindSpeed);
        Bearing runAngle = runAngles.get(trueWindSpeed);
        final double TWO = 2;
        final Bearing TWO_DEGREES = new DegreeBearingImpl(TWO);
        final Bearing MINUS_TWO_DEGREES = new DegreeBearingImpl(-TWO);
        resultWindAngles.add(beatAngle.add(MINUS_TWO_DEGREES).getDegrees());
        resultSpeedsOverGroundInKnots.add(beatVMGPredictionPerTrueWindSpeed.get(trueWindSpeed).getKnots() / Math.cos(beatAngle.add(MINUS_TWO_DEGREES).getRadians()));
        resultWindAngles.add(beatAngle.getDegrees());
        resultSpeedsOverGroundInKnots.add(beatVMGPredictionPerTrueWindSpeed.get(trueWindSpeed).getKnots() / Math.cos(beatAngle.getRadians()));
        for (final Bearing twa : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES) {
            if (twa.compareTo(beatAngle) > 0 && twa.compareTo(runAngle) < 0) {
                resultWindAngles.add(twa.getDegrees());
                resultSpeedsOverGroundInKnots.add(reachingSpeedPredictionsPerTrueWindSpeedAndAngle.get(trueWindSpeed).get(twa).getKnots());
            }
        }
        resultWindAngles.add(runAngle.getDegrees());
        resultSpeedsOverGroundInKnots.add(runVMGPredictionPerTrueWindSpeed.get(trueWindSpeed).getKnots() / Math.cos(Math.PI-runAngle.getRadians()));
        resultWindAngles.add(runAngle.add(TWO_DEGREES).getDegrees());
        resultSpeedsOverGroundInKnots.add(runVMGPredictionPerTrueWindSpeed.get(trueWindSpeed).getKnots() / Math.cos(Math.PI-runAngle.add(TWO_DEGREES).getRadians()));
        return new double[][] {resultWindAngles.stream().mapToDouble(d -> d).toArray(), resultSpeedsOverGroundInKnots.stream().mapToDouble(d -> d).toArray()};
    }
    
    // public accessibility needed for tests, not part of the ORCPerformanceCurve contract
    public Speed getLagrangeSpeedPredictionForTrueWindSpeedAndAngle(Map<Speed, Map<Bearing, Speed>> twaAllowances,
            Map<Speed, Bearing> beatAngles, Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed,
            Map<Speed, Bearing> runAngles, Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed, Speed trueWindSpeed,
            Bearing trueWindAngle) throws FunctionEvaluationException, IllegalArgumentException {
        final Speed result;
        double[][] polarPoints = createPolarsPerTrueWindSpeed(trueWindSpeed, twaAllowances, beatAngles,
                beatVMGPredictionPerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed);
        double[] twaPolarPoints = polarPoints[0];
        double[] speedsOverGroundInKnotsPolarPoints = polarPoints[1];
        int i = -1; // after the loop, i equals the next higher available polar data for the given TWA
        for (int j = 0; j < twaPolarPoints.length; j++) {
            if (trueWindAngle.getDegrees() < twaPolarPoints[j]) {
                i = j;
                break;
            }
        }
        // This part is implemented equally to the part from the ORC PCSLib.pas.
        // ORC decides to use only up to the nearest 4 values to the searched TWA for interpolation,
        // cutting off at the rims, so potentially working only with three values.
        if (i >= 0) {
            int upperBound = Math.min(i + 1, twaPolarPoints.length - 1);
            int lowerBound = Math.max(i - 2, 0);
            double[] xn = new double[upperBound - lowerBound + 1];
            double[] yn = new double[upperBound - lowerBound + 1];
            for (i = lowerBound; i <= upperBound; i++) {
                xn[i - lowerBound] = twaPolarPoints[i];
                yn[i - lowerBound] = speedsOverGroundInKnotsPolarPoints[i];
            }
            result = new KnotSpeedImpl(new PolynomialFunctionLagrangeForm(xn, yn).value(trueWindAngle.getDegrees()));
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Obtains the durations that the boat for which this performance curve was created is expected to sail to complete
     * the {@link #course} (which may be a prefix of a longer course) at the true wind speed provided to the resulting
     * map as the key.
     */
    public Map<Speed, Duration> getAllowancesPerTrueWindSpeedsForCourse() throws ArgumentOutsideDomainException {
        final Map<Speed, Duration> result = new HashMap<>();
        for (final Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            result.put(tws, Duration.ONE_SECOND.times(functionImpliedWindInKnotsToAllowanceInSecondsForCourse.value(tws.getKnots())));
        }
        return result;
    }
    
    @Override
    public String toString() {
        try {
            return "ORCPerformanceCurve [Allowances " + getAllowancesPerTrueWindSpeedsForCourse() + "]";
        } catch (FunctionEvaluationException e) {
            logger.warning("Exception trying to compute string representation of an ORC Performance Curve object: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    
}
