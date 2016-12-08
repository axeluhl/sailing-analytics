package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardDTOCacheInvalidationTest {
    @Test
    public void testFlexibleLeaderboardNameChangeInvalidatesCache()
            throws NoWindException, InterruptedException, ExecutionException {
        final String oldName = "My Flexible Leaderboard";
        final String newName = "My New Leaderboard";
        FlexibleLeaderboard l = new FlexibleLeaderboardImpl(oldName,
                new ThresholdBasedResultDiscardingRuleImpl(new int[0]), new LowPoint(),
                new CourseAreaImpl("My Course Area", UUID.randomUUID()));
        final TimePoint now = MillisecondsTimePoint.now();
        LeaderboardDTO dto = l.getLeaderboardDTO(now, Collections.<String> emptySet(),
                /* addOverallDetails */ false, /* trackedRegattaRegistry */ null, DomainFactory.INSTANCE,
                /* fillTotalPointsUncorrected */ false);
        assertEquals(oldName, dto.name);
        l.setName(newName);
        LeaderboardDTO dtoNew = l.getLeaderboardDTO(now, Collections.<String> emptySet(),
                /* addOverallDetails */ false, /* trackedRegattaRegistry */ null, DomainFactory.INSTANCE,
                /* fillTotalPointsUncorrected */ false);
        assertEquals(newName, dtoNew.name);
    }
}
