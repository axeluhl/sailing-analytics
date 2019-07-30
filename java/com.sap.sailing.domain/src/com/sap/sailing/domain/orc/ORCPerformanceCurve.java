package com.sap.sailing.domain.orc;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

/**
 * For a {@link Competitor} and the {@link ORCPerformanceCurveCourse} which the competitor sailed until the creation of
 * an instance of an implementing class, this instance represents a so called "Perfromance Curve". This Curve is
 * specified by the so called "Implied Wind" a {@link Speed} on the x-Axis and the allowance in s/nm respectively a
 * {@link Duration} on the y-Axis. It represents a simplified polar curve for the given boat and the given part of the
 * course. For a given windspeed the performance curve returns the allowance for the boat or in simpler words: how long
 * should the boat need for a nautical mile when sailing 100% performance.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public interface ORCPerformanceCurve {

    /**
     * TODO Comment
     * 
     * @param durationToCompleteCourse equals the duration the boat needed to conquer the {@link #getCourse() course}
     * @return 
     * 
     * @throws ArgumentOutsideDomainException
     * @throws MaxIterationsExceededException
     * @throws FunctionEvaluationException
     */
    Speed getImpliedWind(Duration durationToCompleteCourse) throws MaxIterationsExceededException, FunctionEvaluationException;
    
    /**
     * TODO getComment done
     * "Scratchboat"
     */
    Duration getCalculatedTime(ORCPerformanceCurve referenceBoat, Duration sailedDurationPerNauticalMile) throws MaxIterationsExceededException, FunctionEvaluationException;
 
    /**
     * The duration that the boat represented by this performance curve is predicted to take to sail the
     * {@link #getCourse() course} at the given {@code trueWindSpeed}.
     */
    Duration getAllowancePerCourse(Speed trueWindSpeed) throws ArgumentOutsideDomainException;
    
    /**
     * 
     * @return the {@link ORCPerformanceCurve}, which is the sailed part of the whole course this PerformanceCurve was
     *         created for
     */
    ORCPerformanceCurveCourse getCourse();
}
