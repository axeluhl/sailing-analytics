package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public class CompetitorResultEditableImpl implements CompetitorResult {
    private static final long serialVersionUID = 3928498127285186791L;

    private Serializable mCompetitorId;
    private int mOneBasedRank;
    private MaxPointsReason mMaxPointsReason;
    private Double mScore;
    private TimePoint mFinishingTime;
    private String mComment;
    private MergeState mMergeState;
    private String mCompetitorName;
    private String mCompetitorShortName;
    private String mBoatName;
    private String mBoatSailId;

    private boolean mDirty;
    private boolean mChecked;

    public CompetitorResultEditableImpl(CompetitorResult result) {
        mCompetitorId = result.getCompetitorId();
        mCompetitorName = result.getName();
        mCompetitorShortName = result.getShortName();
        mBoatName = result.getBoatName();
        mBoatSailId = result.getBoatSailId();
        mOneBasedRank = result.getOneBasedRank();
        mMaxPointsReason = result.getMaxPointsReason();
        mScore = result.getScore();
        mFinishingTime = result.getFinishingTime();
        mComment = result.getComment();
        mMergeState = result.getMergeState();
    }

    public void setValue(CompetitorResult result) {
        mCompetitorId = result.getCompetitorId();
        mCompetitorName = result.getName();
        mCompetitorShortName = result.getShortName();
        mBoatName = result.getBoatName();
        mBoatSailId = result.getBoatSailId();
        mOneBasedRank = result.getOneBasedRank();
        mMaxPointsReason = result.getMaxPointsReason();
        mScore = result.getScore();
        mFinishingTime = result.getFinishingTime();
        mComment = result.getComment();
        mMergeState = result.getMergeState();
    }

    @Override
    public Serializable getCompetitorId() {
        return mCompetitorId;
    }

    public void setCompetitorId(Serializable competitorId) {
        mCompetitorId = competitorId;
    }

    @Override
    public String getName() {
        return mCompetitorName;
    }

    public void setName(String name) {
        mCompetitorName = name;
    }

    @Override
    public String getShortName() {
        return mCompetitorShortName;
    }

    public void setShortName(String shortName) {
        mCompetitorShortName = shortName;
    }

    @Override
    public String getBoatName() {
        return mBoatName;
    }

    public void setBoatName(String name) {
        mBoatName = name;
    }

    @Override
    public String getBoatSailId() {
        return mBoatSailId;
    }

    public void setBoatSailId(String id) {
        mBoatSailId = id;
    }

    @Override
    public int getOneBasedRank() {
        return mOneBasedRank;
    }

    public void setOneBasedRank(int oneBasedRank) {
        mOneBasedRank = oneBasedRank;
    }

    @Override
    public MaxPointsReason getMaxPointsReason() {
        return mMaxPointsReason;
    }

    public void setMaxPointsReason(MaxPointsReason maxPointsReason) {
        mMaxPointsReason = maxPointsReason;
    }

    @Override
    public Double getScore() {
        return mScore;
    }

    public void setScore(Double score) {
        mScore = score;
    }

    @Override
    public TimePoint getFinishingTime() {
        return mFinishingTime;
    }

    public void setFinishingTime(TimePoint finishingTime) {
        mFinishingTime = finishingTime;
    }

    @Override
    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public MergeState getMergeState() {
        return mMergeState == null ? MergeState.OK : mMergeState; // default for having de-serialized an old version
    }

    public void setMergeState(MergeState mergeState) {
        this.mMergeState = mergeState;
    }

    public String getCompetitorDisplayName() {
        if (mCompetitorShortName != null) {
            return mCompetitorShortName + " - " + mCompetitorName;
        }
        return mCompetitorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CompetitorResultEditableImpl that = (CompetitorResultEditableImpl) o;

        if (mOneBasedRank != that.mOneBasedRank)
            return false;
        if (mCompetitorId != null ? !mCompetitorId.equals(that.mCompetitorId) : that.mCompetitorId != null)
            return false;
        if (mCompetitorName != null ? !mCompetitorName.equals(that.mCompetitorName) : that.mCompetitorName != null)
            return false;
        if (mBoatName != null ? !mBoatName.equals(that.mBoatName) : that.mBoatName != null)
            return false;
        if (mBoatSailId != null ? !mBoatSailId.equals(that.mBoatSailId) : that.mBoatSailId != null)
            return false;
        if (mMaxPointsReason != that.mMaxPointsReason)
            return false;
        if (mScore != null ? !mScore.equals(that.mScore) : that.mScore != null)
            return false;
        if (mFinishingTime != null ? !mFinishingTime.equals(that.mFinishingTime) : that.mFinishingTime != null)
            return false;
        return mComment != null ? mComment.equals(that.mComment) : that.mComment == null;

    }

    @Override
    public int hashCode() {
        int result = mCompetitorId != null ? mCompetitorId.hashCode() : 0;
        result = 31 * result + (mCompetitorName != null ? mCompetitorName.hashCode() : 0);
        result = 31 * result + (mBoatName != null ? mBoatName.hashCode() : 0);
        result = 31 * result + (mBoatSailId != null ? mBoatSailId.hashCode() : 0);
        result = 31 * result + mOneBasedRank;
        result = 31 * result + (mMaxPointsReason != null ? mMaxPointsReason.hashCode() : 0);
        result = 31 * result + (mScore != null ? mScore.hashCode() : 0);
        result = 31 * result + (mFinishingTime != null ? mFinishingTime.hashCode() : 0);
        result = 31 * result + (mComment != null ? mComment.hashCode() : 0);
        return result;
    }
}
