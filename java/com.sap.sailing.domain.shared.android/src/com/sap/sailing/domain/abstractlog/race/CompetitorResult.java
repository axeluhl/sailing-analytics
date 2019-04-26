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
    public static enum MergeState {
        /**
         * No merge was necessary at competitor level.
         */
        OK,
        
        /**
         * A field-wise merge was necessary because an update was received that affected this competitor result
         * while locally this competitor result was in a "dirty" (modified) state. The merge was carried out
         * automatically because the fields changed in the dirty state did not overlap with the fields changed
         * in the version of the object received.
         */
        WARNING,
        
        /**
         * A field-wise merge was necessary because an update was received that affected this competitor result while
         * locally this competitor result was in a "dirty" (modified) state. The fields changed in the dirty state
         * overlap with the fields changed in the version of the object received. Field values from the local dirty state
         * have been preferred or have been appended in case of {@link String}-typed fields and need manual
         * intervention.
         */
        ERROR
    };
    
    Serializable getCompetitorId();
    
    String getName();

    String getShortName();

    String getBoatName();

    String getBoatSailId();
    
    int getOneBasedRank();
    
    MaxPointsReason getMaxPointsReason();
    
    Double getScore();
    
    TimePoint getFinishingTime();

    String getComment();
    
    /**
     * Always returns a valid, non-{@code null} result. See {@link MergeState} for details.
     */
    MergeState getMergeState();
}
