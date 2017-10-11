package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.Timed;

/**
 * Represents a bearing step within a certain part of a GPS track. It consists of time point, speed with bearing,
 * and course change in degrees. The latter is calculated as course change between the bearing of the previous step
 * and this step. If there is no previous step, the course change in degrees value should be zero.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface BearingStep extends Timed {

    SpeedWithBearing getSpeedWithBearing();

    double getCourseChangeInDegrees();
}
