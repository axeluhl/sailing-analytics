package com.sap.sailing.server.gateway.expeditionimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.gateway.trackfiles.impl.ExpeditionCourseInferrer;
import com.sap.sailing.server.gateway.trackfiles.impl.ExpeditionStartData;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ExpeditionStartDataTest {
    private static final SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Test
    public void readFile() throws IOException, FormatNotSupportedException, ParseException {
        final String filename = "2018Apr28_05.csv.gz";
        final ExpeditionStartData result = new ExpeditionCourseInferrer(
                RaceLogTrackingAdapterFactory.INSTANCE.getAdapter(DomainFactory.INSTANCE)).getStartData(
                        getClass().getResourceAsStream(filename), filename);
        assertNotNull(result);
        final Set<TimePoint> startTimes = new HashSet<>();
        Util.addAll(result.getStartTimes(), startTimes);
        assertTrue(startTimes.contains(new MillisecondsTimePoint(TIMEPOINT_FORMATTER.parse("2018-04-28T14:44:59.481+0200"))));
        assertTrue(startTimes.contains(new MillisecondsTimePoint(TIMEPOINT_FORMATTER.parse("2018-04-28T14:34:59.260+0200"))));
        assertTrue(startTimes.contains(new MillisecondsTimePoint(TIMEPOINT_FORMATTER.parse("2018-04-28T14:14:58.819+0200"))));
        assertTrue(startTimes.contains(new MillisecondsTimePoint(TIMEPOINT_FORMATTER.parse("2018-04-28T15:29:59.740+0200"))));
        assertTrue(startTimes.contains(new MillisecondsTimePoint(TIMEPOINT_FORMATTER.parse("2018-04-28T14:24:58.953+0200"))));
        assertEquals(34, Util.size(result.getStartLinePortFixes()));
        assertEquals(34, Util.size(result.getStartLineStarboardFixes()));
    }
}
