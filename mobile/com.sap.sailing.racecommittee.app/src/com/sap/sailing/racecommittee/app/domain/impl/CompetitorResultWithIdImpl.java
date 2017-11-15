package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public class CompetitorResultWithIdImpl extends CompetitorResultImpl {
    private static final long serialVersionUID = 4373611281142932067L;
    
    private long mId;

    public CompetitorResultWithIdImpl(long id, CompetitorResult result) {
        this(id, result.getCompetitorId(), result.getCompetitorDisplayName(), result.getOneBasedRank(),
                result.getMaxPointsReason(), result.getScore(), result.getFinishingTime(), result.getComment(),
                result.getMergeState());
    }

    public CompetitorResultWithIdImpl(long id, Serializable competitorId, String competitorDisplayName,
                                      int oneBasedRank, MaxPointsReason maxPointsReason, Double score,
            TimePoint finishingTime, String comment, MergeState mergeState) {
        super(competitorId, competitorDisplayName, oneBasedRank, maxPointsReason, score, finishingTime, comment, mergeState);
        mId = id;
    }

    public long getId() {
        return mId;
    }
}
