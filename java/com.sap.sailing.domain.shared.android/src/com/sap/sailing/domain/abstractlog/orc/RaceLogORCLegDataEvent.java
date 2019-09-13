package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

/**
 * This special {@link RaceLogEvent} is used for setting the needed information for a {@link ORCPerformanceCurveCourse}
 * and {@link ORCPerformanceCurveLeg}, when ranking a race by the usage of a {@link ORCPerformanceCurve} and the
 * constructed course setting.
 * 
 * @author Daniel Lisunkin (i505543)
 */
public interface RaceLogORCLegDataEvent extends RaceLogEvent, Revokable {

    public ORCPerformanceCurveLegTypes getType();
    
    public Distance getLength();

    public Bearing getTwa();

    public int getOneBasedLegNumber();

}
