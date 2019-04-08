package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.TimePoint;

public class CompetitorResultEditableImpl implements CompetitorResult {
    private static final long serialVersionUID = 3928498127285186791L;

    private Serializable mCompetitorId;
    private String mCompetitorDisplayName;
    private int mOneBasedRank;
    private MaxPointsReason mMaxPointsReason;
    private Double mScore;
    private TimePoint mFinishingTime;
    private String mComment;
    private MergeState mMergeState;

    private boolean mDirty;
    private boolean mChecked;

    public CompetitorResultEditableImpl(CompetitorResult result) {
        mCompetitorId = result.getCompetitorId();
        mCompetitorDisplayName = result.getCompetitorDisplayName();
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
    public String getCompetitorDisplayName() {
        return mCompetitorDisplayName;
    }

    public void setCompetitorDisplayName(String competitorDisplayName) {
        mCompetitorDisplayName = competitorDisplayName;
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

    public void setValue(CompetitorResult result) {
        mCompetitorId = result.getCompetitorId();
        mCompetitorDisplayName = result.getCompetitorDisplayName();
        mOneBasedRank = result.getOneBasedRank();
        mMaxPointsReason = result.getMaxPointsReason();
        mScore = result.getScore();
        mFinishingTime = result.getFinishingTime();
        mComment = result.getComment();
        mMergeState = result.getMergeState();
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
        if (mCompetitorDisplayName != null ? !mCompetitorDisplayName.equals(that.mCompetitorDisplayName)
                : that.mCompetitorDisplayName != null)
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
        result = 31 * result + (mCompetitorDisplayName != null ? mCompetitorDisplayName.hashCode() : 0);
        result = 31 * result + mOneBasedRank;
        result = 31 * result + (mMaxPointsReason != null ? mMaxPointsReason.hashCode() : 0);
        result = 31 * result + (mScore != null ? mScore.hashCode() : 0);
        result = 31 * result + (mFinishingTime != null ? mFinishingTime.hashCode() : 0);
        result = 31 * result + (mComment != null ? mComment.hashCode() : 0);
        return result;
    }
}
