package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

/**
 * holds the competitor ID, the competitor name, the finishing rank, an optional score, the optional {@link MaxPointReason} and
 * an optional finishing time, important for handicapped races. An optional comment for the competitor's result can also be
 * provided as a simple {@link String}, such as a reason for a disqualification or a redress given.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CompetitorResult extends Serializable {
    Serializable getCompetitorId();
    
    String getCompetitorDisplayName();
    
    int getOneBasedRank();
    
    MaxPointsReason getMaxPointsReason();
    
    Double getScore();
    
    TimePoint getFinishingTime();

    String getComment();
}
