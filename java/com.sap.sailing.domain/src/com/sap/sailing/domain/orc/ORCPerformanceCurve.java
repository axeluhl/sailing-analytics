package com.sap.sailing.domain.orc;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

// TODO COMMENTS!
public interface ORCPerformanceCurve {

    Speed getImpliedWind(Duration time) throws ArgumentOutsideDomainException, MaxIterationsExceededException, FunctionEvaluationException;
    
    //Comment on "Scratchboat"
    Duration getCalculatedTime(ORCPerformanceCurve referenceBoat, Duration sailedDurationPerNauticalMile);
 
    Duration getAllowancePerCourse(Speed impliedWind);
}
