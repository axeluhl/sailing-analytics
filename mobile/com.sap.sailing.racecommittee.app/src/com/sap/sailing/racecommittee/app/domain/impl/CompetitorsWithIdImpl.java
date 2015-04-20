package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sse.common.Util;

import java.io.Serializable;

public class CompetitorsWithIdImpl {

    private long mId;
    private Serializable mKey;
    private String mText;
    private MaxPointsReason mReason;

    public CompetitorsWithIdImpl(long id, Serializable key, String text, MaxPointsReason reason) {
        mId = id;
        mKey = key;
        mText = text;
        mReason = reason;
    }

    public CompetitorsWithIdImpl(long id, Util.Triple<Serializable, String, MaxPointsReason> triple) {
        mId = id;
        mKey = triple.getA();
        mText = triple.getB();
        mReason = triple.getC();
    }

    public long getId() {
        return mId;
    }

    public Serializable getKey() {
        return mKey;
    }

    public String getText() {
        return mText;
    }

    public MaxPointsReason getReason() {
        return mReason;
    }

    public Util.Triple<Serializable, String, MaxPointsReason> getData() {
        return new Util.Triple<>(mKey, mText, mReason);
    }
}
