package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.ranking.TimeOnTimeAndDistanceRankingMetric;
import com.sap.sailing.domain.tracking.TrackedRace;

public class SerializeRankingMetricTest extends AbstractSerializationTest {
    @Test
    public void testTimeOnTimeAndDistanceRankingMetricSerialization() throws ClassNotFoundException, IOException {
        final TrackedRace trackedRace = mock(TrackedRace.class);
        final TimeOnTimeAndDistanceRankingMetric rankingMetric = new TimeOnTimeAndDistanceRankingMetric(trackedRace);
        TimeOnTimeAndDistanceRankingMetric clone = cloneBySerialization(rankingMetric, DomainFactory.INSTANCE);
        assertNotNull(clone);
    }
}
