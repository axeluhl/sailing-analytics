package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.ConfirmedFinishPositioningListFinder;

@SuppressWarnings("unchecked")
public class ConfirmedFinishPositioningListFinderTest extends
        PassAwareRaceLogAnalyzerTest<ConfirmedFinishPositioningListFinder, List<Triple<Serializable, String, MaxPointsReason>>> {
    
    @Override
    protected ConfirmedFinishPositioningListFinder createAnalyzer(RaceLog raceLog) {
        return new ConfirmedFinishPositioningListFinder(raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, RaceLogEventAuthor author) {
        RaceLogFinishPositioningConfirmedEvent event = createEvent(RaceLogFinishPositioningConfirmedEvent.class, 1, passId, author);
        when(event.getPositionedCompetitorsIDsNamesMaxPointsReasons()).thenReturn(mock(List.class));
        return new TargetPair(Arrays.asList(event), event.getPositionedCompetitorsIDsNamesMaxPointsReasons());
    }

    @Test
    public void testNullForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertNull(analyzer.analyze());
    }

    @Test
    public void testMostRecent() {
        RaceLogFinishPositioningConfirmedEvent event1 = createEvent(RaceLogFinishPositioningConfirmedEvent.class, 1);
        RaceLogFinishPositioningConfirmedEvent event2 = createEvent(RaceLogFinishPositioningConfirmedEvent.class, 2);
        when(event2.getPositionedCompetitorsIDsNamesMaxPointsReasons()).thenReturn(mock(List.class));

        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getPositionedCompetitorsIDsNamesMaxPointsReasons(), analyzer.analyze());
    }
}
