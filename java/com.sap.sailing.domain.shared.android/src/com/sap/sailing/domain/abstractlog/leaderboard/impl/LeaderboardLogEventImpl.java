package com.sap.sailing.domain.abstractlog.leaderboard.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogEvent;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogEventVisitor;
import com.sap.sse.common.TimePoint;

public abstract class LeaderboardLogEventImpl extends AbstractLogEventImpl<LeaderboardLogEventVisitor> implements LeaderboardLogEvent {

    private static final long serialVersionUID = -2557594972618769182L;

    public LeaderboardLogEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId) {
        super(createdAt, author, logicalTimePoint, pId);
    }
}
