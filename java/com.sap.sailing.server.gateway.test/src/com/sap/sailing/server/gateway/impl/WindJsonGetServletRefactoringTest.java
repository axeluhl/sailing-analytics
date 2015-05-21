package com.sap.sailing.server.gateway.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.CombinedWindTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * After refactoring the {@link WindJsonGetServlet} to first copy the relevant fixes to hold the read lock only
 * for a short time, this test uses the old implementation to compute the expected result and compares the
 * output received from the servlet.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindJsonGetServletRefactoringTest {
    private WindJsonGetServlet servlet;
    
    @Before
    public void setUp() {
        servlet = new WindJsonGetServlet();
    }
    
    private JSONObject getExpectedResult(String windSourceToRead, TrackedRace trackedRace, TimePoint from, TimePoint to) {
        JSONObject jsonWindTracks = new JSONObject();
        List<WindSource> windSources = servlet.getAvailableWindSources(trackedRace);
        JSONArray jsonWindSourcesDisplayed = new JSONArray();
        for (WindSource windSource : windSources) {
            JSONObject windSourceInformation = new JSONObject();
            windSourceInformation.put("typeName", windSource.getType().name());
            windSourceInformation.put("id", windSource.getId() != null ? windSource.getId().toString() : "");
            jsonWindSourcesDisplayed.add(windSourceInformation);
        }
        jsonWindTracks.put("availableWindSources", jsonWindSourcesDisplayed);
        for (WindSource windSource : windSources) {
            if("ALL".equals(windSourceToRead) || windSource.getType().name().equalsIgnoreCase(windSourceToRead)) {
                JSONArray jsonWindArray = new JSONArray();
                WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                windTrack.lockForRead();
                try {
                    Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
                    while (windIter.hasNext()) {
                        Wind wind = windIter.next();
                        if (wind.getTimePoint().compareTo(to) > 0) {
                            break;
                        }
                        JSONObject jsonWind = new JSONObject();
                        jsonWind.put("truebearingdeg", wind.getBearing().getDegrees());
                        jsonWind.put("knotspeed", wind.getKnots());
                        jsonWind.put("meterspersecondspeed", wind.getMetersPerSecond());
                        if (wind.getTimePoint() != null) {
                            jsonWind.put("timepoint", wind.getTimePoint().asMillis());
                            jsonWind.put("dampenedtruebearingdeg",
                                    windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint())
                                    .getBearing().getDegrees());
                            jsonWind.put("dampenedknotspeed",
                                    windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint()).getKnots());
                            jsonWind.put("dampenedmeterspersecondspeed",
                                    windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint())
                                    .getMetersPerSecond());
                        }
                        if (wind.getPosition() != null) {
                            jsonWind.put("latdeg", wind.getPosition().getLatDeg());
                            jsonWind.put("lngdeg", wind.getPosition().getLngDeg());
                        }
                        jsonWindArray.add(jsonWind);
                    }
                } finally {
                    windTrack.unlockAfterRead();
                }
                jsonWindTracks.put(windSource.toString()+(windSource.getId()!=null ? "-"+windSource.getId():""), jsonWindArray);
            }
        }
        return jsonWindTracks;
    }
    
    @Test
    public void compareOldAndNewImplementation() {
        TrackedRace trackedRace = mock(TrackedRace.class);
        final WindSourceImpl webWindSource = new WindSourceImpl(WindSourceType.WEB);
        when(trackedRace.getWindSources()).thenReturn(new HashSet<>(Arrays.asList(new WindSource[] { webWindSource })));
        RaceDefinition race = mock(RaceDefinition.class);
        when(race.getName()).thenReturn("Race Name");
        when(trackedRace.getRace()).thenReturn(race);
        Set<WindSource> noWindSourcesToExclude = Collections.emptySet();
        when(trackedRace.getWindSourcesToExclude()).thenReturn(noWindSourcesToExclude);
        WindTrackImpl webWindTrack = new WindTrackImpl(/* millisecondsOverWhichToAverage */ 30000, /* useSpeed */ true, /* nameForReadWriteLock */ "compareOldAndNewImplementation");
        when(trackedRace.getOrCreateWindTrack(webWindSource)).thenReturn(webWindTrack);
        WindSourceImpl combinedWindSource = new WindSourceImpl(WindSourceType.COMBINED);
        CombinedWindTrackImpl combinedWindTrack = new CombinedWindTrackImpl(trackedRace, /* baseConfidence */ 0.5);
        when(trackedRace.getOrCreateWindTrack(combinedWindSource)).thenReturn(combinedWindTrack);
        final TimePoint now = MillisecondsTimePoint.now();
        final TimePoint earlier = now.minus(10000);
        final TimePoint later = now.plus(10000);
        final Matcher<TimePoint> isTooEarlyOrTooLate = new ArgumentMatcher<TimePoint>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof TimePoint &&
                        ((TimePoint) item).before(earlier);
            }
        };
        final Matcher<TimePoint> isNeitherTooEarlyNorTooLate = new ArgumentMatcher<TimePoint>() {
            @Override
            public boolean matches(Object argument) {
                return !isTooEarlyOrTooLate.matches(argument);
            }
        };
        stub(trackedRace.getWind(isA(Position.class), argThat(isTooEarlyOrTooLate))).toReturn(null);
        Wind someCombinedWindFix = new WindImpl(new DegreePosition(50, 4), now,
                new KnotSpeedWithBearingImpl(15, new DegreeBearingImpl(234)));
        stub(trackedRace.getWind(isA(Position.class), argThat(isNeitherTooEarlyNorTooLate))).toReturn(someCombinedWindFix);
        webWindTrack.add(new WindImpl(new DegreePosition(49, 3), now,
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(123))));
        System.out.println(getExpectedResult(WindJsonGetServlet.ALL, trackedRace, earlier, later));
        assertWindSourcesEquals(getExpectedResult(WindJsonGetServlet.ALL, trackedRace, earlier, later),
                servlet.getResult(WindJsonGetServlet.ALL, "", trackedRace, earlier, later));
    }

    private void assertWindSourcesEquals(JSONObject expectedResult, JSONObject result) {
        Set<String> expectedWindSourceNames = ((JSONArray) expectedResult.get("availableWindSources")).stream().map(o->o.toString()).collect(Collectors.toSet());
        Set<String> actualWindSourceNames = ((JSONArray) result.get("availableWindSources")).stream().map(o->o.toString()).collect(Collectors.toSet());
        assertEquals(expectedWindSourceNames, actualWindSourceNames);
        for (String windSourceName : expectedWindSourceNames) {
            assertEquals(expectedResult.get(windSourceName), result.get(windSourceName));
        }
    }
}
