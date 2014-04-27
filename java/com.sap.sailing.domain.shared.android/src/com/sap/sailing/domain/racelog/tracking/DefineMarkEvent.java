package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

/**
 * Defines a mark, that can then be used to build a course.
 * @author Fredrik Teschke
 *
 */
public interface DefineMarkEvent extends RaceLogEvent, Revokable {
    Mark getMark();
}
