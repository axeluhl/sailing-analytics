package com.sap.sailing.polars.caching.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.caching.PolarFixCacheRaceInterval;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class PolarFixCacheRaceIntervalTest {
	
	@Test
	public void testIntervalJoin() {
		TrackedRace mockedTrackedRace1 = mock(TrackedRace.class);
		TrackedRace mockedTrackedRace2 = mock(TrackedRace.class);

		Competitor mockedCompetitor1 = mock(Competitor.class);
		Competitor mockedCompetitor2 = mock(Competitor.class);

		Calendar calender = Calendar.getInstance();
		calender.set(2014, 3, 17, 16, 0, 0);
		TimePoint timePoint1 = new MillisecondsTimePoint(calender.getTime());
		calender.set(2014, 3, 17, 16, 10, 0);
		TimePoint timePoint2 = new MillisecondsTimePoint(calender.getTime());

		PolarFixCacheRaceInterval interval1 = createInterval1(
				mockedTrackedRace1, mockedTrackedRace2, mockedCompetitor1,
				mockedCompetitor2, timePoint1, timePoint2);

		PolarFixCacheRaceInterval interval2 = createInterval2(
				mockedTrackedRace1, mockedTrackedRace2, mockedCompetitor1,
				timePoint2);
		PolarFixCacheRaceInterval joined = interval1.join(interval2);

		Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> joinedMap = joined
				.getCompetitorAndTimepointsForRace();
		
		Map<Competitor, Pair<TimePoint, TimePoint>> explicitIntervalForRace1 = joinedMap.get(mockedTrackedRace1);
		assertThat(explicitIntervalForRace1, notNullValue(Map.class));
		Pair<TimePoint, TimePoint> explicitIntervalForCompetitor1 = explicitIntervalForRace1.get(mockedCompetitor1);
		assertThat(explicitIntervalForCompetitor1, notNullValue(Pair.class));
		
		TimePoint start1 = explicitIntervalForCompetitor1.getA();
		TimePoint end1 = explicitIntervalForCompetitor1.getB();
		
		assertThat(start1, is(timePoint1));
		assertThat(end1, is(timePoint2));
		
	}

	private PolarFixCacheRaceInterval createInterval1(
			TrackedRace mockedTrackedRace1, TrackedRace mockedTrackedRace2,
			Competitor mockedCompetitor1, Competitor mockedCompetitor2,
			TimePoint timePoint1, TimePoint timePoint2) {
		Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> competitorAndTimepointsForRace = new HashMap<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>>();
		HashMap<Competitor, Pair<TimePoint, TimePoint>> mapForRace1 = new HashMap<Competitor, Pair<TimePoint, TimePoint>>();
		mapForRace1.put(mockedCompetitor1, new Pair<TimePoint, TimePoint>(timePoint1, timePoint1));
		competitorAndTimepointsForRace.put(mockedTrackedRace1, mapForRace1);
		HashMap<Competitor, Pair<TimePoint, TimePoint>> mapForRace2 = new HashMap<Competitor, Pair<TimePoint, TimePoint>>();
		mapForRace2.put(mockedCompetitor2, new Pair<TimePoint, TimePoint>(timePoint1, timePoint2));
		competitorAndTimepointsForRace.put(mockedTrackedRace2, mapForRace2);
		PolarFixCacheRaceInterval interval1 = new PolarFixCacheRaceInterval(
				competitorAndTimepointsForRace);
		return interval1;
	}
	
	private PolarFixCacheRaceInterval createInterval2(
			TrackedRace mockedTrackedRace1, TrackedRace mockedTrackedRace2,
			Competitor mockedCompetitor1, TimePoint timePoint2) {
		Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> competitorAndTimepointsForRace = new HashMap<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>>();
		HashMap<Competitor, Pair<TimePoint, TimePoint>> mapForRace1 = new HashMap<Competitor, Pair<TimePoint, TimePoint>>();
		mapForRace1.put(mockedCompetitor1, new Pair<TimePoint, TimePoint>(
				timePoint2, timePoint2));
		competitorAndTimepointsForRace.put(mockedTrackedRace1, mapForRace1);
		PolarFixCacheRaceInterval interval2 = new PolarFixCacheRaceInterval(
				competitorAndTimepointsForRace);
		return interval2;
	}
}
