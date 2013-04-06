package com.sap.sailing.domain.racelog;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Pair;

public interface RaceLogFinishPositioningListChangedEvent extends RaceLogEvent {
    
    List<Pair<Competitor, MaxPointsReason>> getPositionedCompetitors();
}
