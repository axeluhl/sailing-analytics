package com.sap.sailing.domain.orc;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;

import com.sap.sailing.domain.base.Competitor;
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
     * 
     * @param time
     * @return
     * @throws ArgumentOutsideDomainException
     * @throws MaxIterationsExceededException
     * @throws FunctionEvaluationException
     */
    Speed getImpliedWind(Duration time) throws ArgumentOutsideDomainException, MaxIterationsExceededException, FunctionEvaluationException;
    
    /**
     * TODO getComment done
     * "Scratchboat"
     * 
     * @param referenceBoat
     * @param sailedDurationPerNauticalMile
     * @return
     */
    Duration getCalculatedTime(ORCPerformanceCurve referenceBoat, Duration sailedDurationPerNauticalMile);
 
    /**
     * 
     * @param impliedWind
     * @return
     */
    Duration getAllowancePerCourse(Speed impliedWind);
}
