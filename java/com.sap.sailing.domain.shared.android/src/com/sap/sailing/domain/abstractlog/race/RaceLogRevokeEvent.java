package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.RevokeEvent;

public interface RaceLogRevokeEvent extends RaceLogEvent, RevokeEvent<RaceLogEventVisitor> {

}
