package com.sap.sailing.datamining.impl.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.data.LeaderboardGroupWithContext;
import com.sap.sailing.datamining.impl.data.HasLeaderboardGroupContextImpl;
import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContextImpl;
import com.sap.sailing.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public class TestLeaderboardGroupFilteringRetrievalProcessor {
    
    private RacingEventService service;
    private Processor<RacingEventService> retriever;
    
    private Collection<LeaderboardGroupWithContext> retrievedGroups;
    
    @Before
    public void initializeComponents() {
        service = mock(RacingEventService.class);
        stub(service.getLeaderboardGroups()).toReturn(getGroupsInService());

        retrievedGroups = new HashSet<>();
        Processor<LeaderboardGroupWithContext> receiver = new Processor<LeaderboardGroupWithContext>() {
            @Override
            public void onElement(LeaderboardGroupWithContext element) {
                retrievedGroups.add(element);
            }
            @Override
            public void onFailure(Throwable failure) {
            }
            @Override
            public void finish() throws InterruptedException {
            }
            @Override
            public void abort() {
            }
            @Override
            public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
                return null;
            }
        };
        
        FilterCriteria<LeaderboardGroupWithContext> criteria = new FilterCriteria<LeaderboardGroupWithContext>() {
            @Override
            public boolean matches(LeaderboardGroupWithContext element) {
                String name = element.getLeaderboardGroupName();
                return name == "LG1" || name == "LG2" || name == "LG3";
            }
        };
        retriever = new LeaderboardGroupRetrievalProcessor(ConcurrencyTestsUtil.getExecutor(), Arrays.asList(receiver), criteria);
    }

    private Map<String, LeaderboardGroup> getGroupsInService() {
        Map<String, LeaderboardGroup> groups = new HashMap<>();
        groups.put("LG1", new LeaderboardGroupImpl("LG1", "", false, new ArrayList<Leaderboard>()));
        groups.put("LG2", new LeaderboardGroupImpl("LG2", "", false, new ArrayList<Leaderboard>()));
        groups.put("LG3", new LeaderboardGroupImpl("LG3", "", false, new ArrayList<Leaderboard>()));
        groups.put("LG4", new LeaderboardGroupImpl("LG4", "", false, new ArrayList<Leaderboard>()));
        groups.put("LG5", new LeaderboardGroupImpl("LG5", "", false, new ArrayList<Leaderboard>()));
        return groups;
    }

    @Ignore
    @Test
    public void testFilteringRetrieval() throws InterruptedException {
        retriever.onElement(service);
        retriever.finish();
        
        Collection<LeaderboardGroupWithContext> expectedGroups = getExpectedRetrievedGroups();
        assertThat(retrievedGroups, is(expectedGroups));
    }

    private Collection<LeaderboardGroupWithContext> getExpectedRetrievedGroups() {
        Collection<LeaderboardGroupWithContext> groups = new HashSet<>();
        groups.add(groupToGroupWithContext(new LeaderboardGroupImpl("LG1", "", false, new ArrayList<Leaderboard>())));
        groups.add(groupToGroupWithContext(new LeaderboardGroupImpl("LG2", "", false, new ArrayList<Leaderboard>())));
        groups.add(groupToGroupWithContext(new LeaderboardGroupImpl("LG3", "", false, new ArrayList<Leaderboard>())));
        groups.add(groupToGroupWithContext(new LeaderboardGroupImpl("LG4", "", false, new ArrayList<Leaderboard>())));
        groups.add(groupToGroupWithContext(new LeaderboardGroupImpl("LG5", "", false, new ArrayList<Leaderboard>())));
        return groups;
    }

    private LeaderboardGroupWithContext groupToGroupWithContext(LeaderboardGroupImpl leaderboardGroup) {
        HasLeaderboardGroupContext context = new HasLeaderboardGroupContextImpl(leaderboardGroup);
        return new LeaderboardGroupWithContextImpl(context);
    }

}
