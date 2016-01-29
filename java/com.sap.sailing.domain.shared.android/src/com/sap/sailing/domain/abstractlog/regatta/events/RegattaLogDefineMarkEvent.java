package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Mark;

/**
 * Defines a mark, that can then be used to build a course.
 * 
 */
public interface RegattaLogDefineMarkEvent extends RegattaLogEvent {
    Mark getMark();
}
