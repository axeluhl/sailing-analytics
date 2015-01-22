package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Mark;

/**
 * Defines a mark, that can then be used to build a course.
 * @author Fredrik Teschke
 *
 */
public interface RaceLogDefineMarkEvent extends RaceLogEvent, Revokable {
    Mark getMark();
}
