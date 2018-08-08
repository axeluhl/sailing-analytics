package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public class CompetitorResultImpl implements CompetitorResult {
    private static final long serialVersionUID = 4928351242700897387L;

    private final Serializable competitorId;
    
    private final String competitorName;

    private final String competitorShortName;
    
    private final int oneBasedRank;
    
    private final MaxPointsReason maxPointsReason;
    
    private final Double score;
    
    private final TimePoint finishingTime;
    
    private final String comment;

    private final MergeState mergeState;

    private final String boatName;

    private final String boatSailId;

    public CompetitorResultImpl(CompetitorResult item) {
        this(item.getCompetitorId(), item.getName(), item.getShortName(), item.getBoatName(), item.getBoatSailId(),
                item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                item.getComment(), item.getMergeState());
    }

    public CompetitorResultImpl(Serializable competitorId, String competitorName, String competitorShortName,
            String boatName, String boatSailId, int oneBasedRank, MaxPointsReason maxPointsReason, Double score,
            TimePoint finishingTime, String comment, MergeState mergeState) {
        super();
        this.competitorId = competitorId;
        this.competitorName = competitorName;
        this.competitorShortName = competitorShortName;
        this.boatName = boatName;
        this.boatSailId = boatSailId;
        this.oneBasedRank = oneBasedRank;
        this.maxPointsReason = maxPointsReason;
        this.score = score;
        this.finishingTime = finishingTime;
        this.comment = comment;
        this.mergeState = mergeState;
    }

    @Override
    public Serializable getCompetitorId() {
        return competitorId;
    }

    @Override
    public String getName() {
        return competitorName;
    }

    @Override
    public String getShortName() {
        return competitorShortName;
    }

    @Override
    public String getBoatName() {
        return boatName;
    }

    @Override
    public String getBoatSailId() {
        return boatSailId;
    }

    @Override
    public int getOneBasedRank() {
        return oneBasedRank;
    }

    @Override
    public MaxPointsReason getMaxPointsReason() {
        return maxPointsReason;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public TimePoint getFinishingTime() {
        return finishingTime;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public MergeState getMergeState() {
        return mergeState == null ? MergeState.OK : mergeState; // default in case of having de-serialized an old version
    }

    public String getCompetitorDisplayName() {
        if (competitorShortName != null) {
            return competitorShortName + " - " + competitorName;
        }
        return competitorName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((competitorName == null) ? 0 : competitorName.hashCode());
        result = prime * result + ((competitorShortName == null) ? 0 : competitorShortName.hashCode());
        result = prime * result + ((competitorId == null) ? 0 : competitorId.hashCode());
        result = prime * result + ((boatName == null) ? 0 : boatName.hashCode());
        result = prime * result + ((boatSailId == null) ? 0 : boatSailId.hashCode());
        result = prime * result + ((finishingTime == null) ? 0 : finishingTime.hashCode());
        result = prime * result + ((maxPointsReason == null) ? 0 : maxPointsReason.hashCode());
        result = prime * result + oneBasedRank;
        result = prime * result + ((score == null) ? 0 : score.hashCode());
        result = prime * result + ((mergeState == null) ? 0 : mergeState.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompetitorResultImpl other = (CompetitorResultImpl) obj;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (competitorName == null) {
            if (other.competitorName != null)
                return false;
        } else if (!competitorName.equals(other.competitorName))
            return false;
        if (competitorId == null) {
            if (other.competitorId != null)
                return false;
        } else if (!competitorId.equals(other.competitorId))
            return false;
        if (boatName == null) {
            if (other.boatName != null)
                return false;
        } else if (!boatName.equals(other.boatName))
            return false;
        if (boatSailId == null) {
            if (other.boatSailId != null)
                return false;
        } else if (!boatSailId.equals(other.boatSailId))
            return false;
        if (finishingTime == null) {
            if (other.finishingTime != null)
                return false;
        } else if (!finishingTime.equals(other.finishingTime))
            return false;
        if (maxPointsReason != other.maxPointsReason)
            return false;
        if (oneBasedRank != other.oneBasedRank)
            return false;
        if (score == null) {
            if (other.score != null)
                return false;
        } else if (!score.equals(other.score))
            return false;
        if (mergeState == null) {
            if (other.mergeState != null)
                return false;
        } else if (!mergeState.equals(other.mergeState))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CompetitorResultImpl [competitorId=" + competitorId + ", competitorName=" + competitorName
                + ", competitorShortName=" + competitorShortName + ", boatName=" + boatName+ ", boatSailId="
                + boatSailId + ", rank=" + oneBasedRank + ", maxPointsReason=" + maxPointsReason + ", score="+ score
                + ", finishingTime=" + finishingTime + ", comment=" + comment + ", mergeState=" + mergeState + "]";
    }
    
}
