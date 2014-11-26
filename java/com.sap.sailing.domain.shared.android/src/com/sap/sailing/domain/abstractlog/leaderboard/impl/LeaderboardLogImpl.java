package com.sap.sailing.domain.abstractlog.leaderboard.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventComparator;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLog;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogEvent;
import com.sap.sailing.domain.abstractlog.leaderboard.LeaderboardLogEventVisitor;

public class LeaderboardLogImpl extends AbstractLogImpl<LeaderboardLogEvent, LeaderboardLogEventVisitor> implements LeaderboardLog {

    private static final long serialVersionUID = 98032278604708475L;

    public LeaderboardLogImpl(Serializable identifier) {
        super(identifier, new LogEventComparator());
    }

    public LeaderboardLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier, new LogEventComparator());
    }

    @Override
    protected LeaderboardLogEvent createRevokeEvent(AbstractLogEventAuthor author, LeaderboardLogEvent toRevoke, String reason) {
        return new LeaderboardLogRevokeEventImpl(author, toRevoke, reason);
    }
}
