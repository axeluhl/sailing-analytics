package com.sap.sailing.domain.abstractlog.leaderboard;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sse.common.WithID;

public interface LeaderboardLog extends AbstractLog<LeaderboardLogEvent, LeaderboardLogEventVisitor>, WithID {

}
