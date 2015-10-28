package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.DefineMarkEvent;
import com.sap.sailing.domain.base.Mark;

/**
 * Defines a mark, that can then be used to build a course.
 * 
 * @author Fredrik Teschke
 *
 */
@Deprecated //see bug 2851
public interface RaceLogDefineMarkEvent extends DefineMarkEvent<RaceLogEventVisitor>, RaceLogEvent, Revokable {
    Mark getMark();
}
