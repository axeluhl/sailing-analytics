package com.sap.sailing.domain.racelog;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;

public abstract interface RaceLogFinishPositioningEvent extends RaceLogEvent {
    
    /**
     * @return a triple holding the competitor ID, the competitor name and the {@link MaxPointReason} documenting the
     *         score for the competitor
     */
    List<Triple<Serializable, String, MaxPointsReason>> getPositionedCompetitorsIDsNamesMaxPointsReasons();
}
