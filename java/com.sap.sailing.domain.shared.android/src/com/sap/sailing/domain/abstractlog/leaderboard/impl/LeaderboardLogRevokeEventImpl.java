package com.sap.sailing.domain.abstractlog.leaderboard.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.RevokeEventImpl;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogEvent;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogEventVisitor;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogRevokeEvent;
import com.sap.sse.common.TimePoint;

public class LeaderboardLogRevokeEventImpl extends RevokeEventImpl<LeaderboardLogEventVisitor> implements LeaderboardLogRevokeEvent {
    private static final long serialVersionUID = -3470191515219206588L;
    
    public LeaderboardLogRevokeEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo,
            String reason) {
        super(createdAt, author, logicalTimePoint, pId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }
    
    public LeaderboardLogRevokeEventImpl(AbstractLogEventAuthor author, LeaderboardLogEvent toRevoke, String reason) {
        super(author, toRevoke, reason);
    }

    @Override
    public void accept(LeaderboardLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
