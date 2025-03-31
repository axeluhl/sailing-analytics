package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;

public interface RaceLogEvent extends AbstractLogEvent<RaceLogEventVisitor>, RaceLogEventData {

}
