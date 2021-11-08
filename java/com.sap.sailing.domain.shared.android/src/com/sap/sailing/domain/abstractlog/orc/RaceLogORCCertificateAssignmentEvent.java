package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.race.InvalidatesLeaderboardCache;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;

public interface RaceLogORCCertificateAssignmentEvent extends RaceLogEvent, ORCCertificateAssignmentEvent<RaceLogEventVisitor>, InvalidatesLeaderboardCache {
}
