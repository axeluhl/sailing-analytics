package com.sap.sailing.gwt.ui.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLogImpl;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.gwt.ui.adminconsole.RaceLogSetTrackingTimesDTO;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.CountryCodeFactoryImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.ClearStateTestSupport;

import org.junit.Assert;

public class SlicingTest {
    private final String regattaName = "test";
    private final String boatClassName = BoatClassMasterdata.TP52.name();
    private final TimePoint startOriginalRace = new MillisecondsTimePoint(0);
    private final TimePoint endOriginalRace = new MillisecondsTimePoint(300);
    private final TimePoint startSlicedRace = new MillisecondsTimePoint(100);
    private final TimePoint endSlicedRace = new MillisecondsTimePoint(200);
    private final String seriesName = LeaderboardNameConstants.DEFAULT_SERIES_NAME;
    private final String fleetName = "default";
    private final String columnNameOriginalRace = "original";
    private final String columnNameSlicedRace = "sliced";
    
    private final CompetitorDescriptor competitor = new CompetitorDescriptor(null, regattaName, columnNameOriginalRace, fleetName, null, 
            "test1", "test", "test", null, CountryCodeFactoryImpl.INSTANCE.getFromTwoLetterISOName("de"), 
            1.0, null, null, "B1", boatClassName, "GER 123");

    private SailingServiceImplMock sailingService;
    
    private ThreadState threadState;
    protected Subject subject;

    @Before
    public void setUp() throws Exception {
        sailingService = new SailingServiceImplMock();
        ((ClearStateTestSupport)(sailingService.getRacingEventService())).clearState();
        
        subject = Mockito.mock(Subject.class);
        threadState = new SubjectThreadState(subject);
        threadState.bind();
    }
    
    @After
    public void tearDown() throws Exception {
        threadState.clear();
        ((ClearStateTestSupport)(sailingService.getRacingEventService())).clearState();
    }

    @Test
    @Ignore
    /**
     * Currently non functional test case for race slicing. Should be finished when SailingServiceImplMock is fixed to
     * work properly for test cases. See bug 4418 for details.
     */
    public void testSliceRace() throws Exception {
        final LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters = new LinkedHashMap<>();
        final List<FleetDTO> fleets = new ArrayList<>();
        fleets.add(new FleetDTO(fleetName, 0, null));
        seriesCreationParameters.put(seriesName,
                new SeriesCreationParametersDTO(fleets, false, false, true, false, new int[0], false, 0));
        final RegattaCreationParametersDTO regattaCreationParameters = new RegattaCreationParametersDTO(
                seriesCreationParameters);
        final RegattaDTO regatta = sailingService.createRegatta(regattaName, boatClassName, false,
                CompetitorRegistrationType.CLOSED, null, null, null, regattaCreationParameters, false,
                ScoringSchemeType.HIGH_POINT, null, 3.0, false, false, RankingMetrics.ONE_DESIGN);
        final List<Pair<String, Integer>> columnNames = new ArrayList<>();
        columnNames.add(new Pair<>(columnNameOriginalRace, 0));
        sailingService.addRaceColumnsToSeries(regatta.getRegattaIdentifier(), seriesName, columnNames);
        sailingService.createRegattaLeaderboard(new RegattaName(regatta.getName()), regattaName, new int[0]);
        final List<CompetitorWithBoatDTO> competitors = sailingService.addCompetitors(Arrays.asList(competitor), null);
        final CompetitorWithBoatDTO competitorDTO = competitors.iterator().next();
        sailingService.setCompetitorRegistrationsInRegattaLog(regattaName, new HashSet<>(competitors));
        DeviceIdentifierDTO deviceId = new DeviceIdentifierDTO("FILE", UUID.randomUUID().toString());
        final DeviceMappingDTO deviceMapping = new DeviceMappingDTO(deviceId, startOriginalRace.asDate(), endOriginalRace.asDate(), competitorDTO, null);
        sailingService.addDeviceMappingToRegattaLog(regattaName, deviceMapping);
        final List<Triple<String, String, String>> leaderboardRaceColumnFleetNames = new ArrayList<>();
        leaderboardRaceColumnFleetNames.add(new Triple<>(regattaName, columnNameOriginalRace, fleetName));
        RaceLogSetTrackingTimesDTO raceLogSetTrackingTimes = new RaceLogSetTrackingTimesDTO();
        raceLogSetTrackingTimes.leaderboardName = regattaName;
        raceLogSetTrackingTimes.authorName = "Test";
        raceLogSetTrackingTimes.raceColumnName = columnNameOriginalRace;
        raceLogSetTrackingTimes.fleetName = fleetName;
        raceLogSetTrackingTimes.newStartOfTracking = new TimePointSpecificationFoundInLogImpl(startOriginalRace);
        raceLogSetTrackingTimes.newEndOfTracking = new TimePointSpecificationFoundInLogImpl(endOriginalRace);
        sailingService.setTrackingTimes(raceLogSetTrackingTimes);
        sailingService.startRaceLogTracking(leaderboardRaceColumnFleetNames , true, false);
        // TODO add wind fixes
        
        final StrippedLeaderboardDTO leaderboard = sailingService.getLeaderboard(regattaName);
        final RaceColumnDTO columnOfOriginalRace = leaderboard.getRaceList().iterator().next();
        final RaceDTO originalRace = columnOfOriginalRace.getRace(columnOfOriginalRace.getFleets().iterator().next());
        final RegattaAndRaceIdentifier raceIdentifierOfOriginalRace = originalRace.getRaceIdentifier();
        final RegattaAndRaceIdentifier sliceRaceIdentifier = sailingService.sliceRace(raceIdentifierOfOriginalRace, columnNameSlicedRace, startSlicedRace, endSlicedRace);
        final StrippedLeaderboardDTO leaderboard2 = sailingService.getLeaderboard(regattaName);
        final RaceColumnDTO columnOfSlicedRace = leaderboard2.getRaceColumnByName(columnNameSlicedRace);
        final RaceDTO slicedRace = columnOfSlicedRace.getRace(columnOfOriginalRace.getFleets().iterator().next());
        Assert.assertEquals(slicedRace.getRaceIdentifier(), sliceRaceIdentifier);
        final TrackedRaceDTO trackedRace = slicedRace.trackedRace;
        Assert.assertEquals(startSlicedRace.asDate(), trackedRace.startOfTracking);
        Assert.assertEquals(endSlicedRace.asDate(), trackedRace.endOfTracking);
    }

}
