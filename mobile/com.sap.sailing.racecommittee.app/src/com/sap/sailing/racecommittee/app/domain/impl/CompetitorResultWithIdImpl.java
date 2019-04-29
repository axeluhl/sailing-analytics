package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public class CompetitorResultWithIdImpl extends CompetitorResultImpl {
    private static final long serialVersionUID = 4373611281142932067L;

    private long mId;
    private Boat mBoat;

    public CompetitorResultWithIdImpl(long id, Boat boat, CompetitorResult result) {
        this(id, boat, result.getCompetitorId(), result.getName(), result.getShortName(),
                result.getOneBasedRank(), result.getMaxPointsReason(), result.getScore(), result.getFinishingTime(),
                result.getComment(), result.getMergeState());
    }

    public CompetitorResultWithIdImpl(long id, Boat boat, Serializable competitorId, String competitorName,
            String shortName, int oneBasedRank, MaxPointsReason maxPointsReason, Double score, TimePoint finishingTime,
            String comment, MergeState mergeState) {
        super(competitorId, competitorName, shortName,
                boat == null ? null : boat.getName(), boat == null ? null : boat.getSailID(),
                oneBasedRank, maxPointsReason, score, finishingTime, comment, mergeState);

        mId = id;
        mBoat = boat;
    }

    public long getId() {
        return mId;
    }

    public Boat getBoat() {
        return mBoat;
    }

    public void setBoat(Boat boat) {
        mBoat = boat;
    }
}
