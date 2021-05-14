package com.sap.sailing.server.test;

import java.util.Collections;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.server.hierarchy.SailingHierarchyOwnershipUpdater;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.testsupport.SecurityBundleTestWrapper;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.UserManagementException;

public class LeagueEventHierarchyOwnershipChangeTest {
    private Event event;
    private LeaderboardGroup leaderboardGroup;
    private Leaderboard overallLeaderboard;
    private RacingEventService service;
    private Subject subject;
    private SubjectThreadState threadState;
    private SecurityService securityService;

    @Before
    public void setUp() throws Exception {
        MongoDBConfiguration.getDefaultTestConfiguration().getService().getDB().drop();
        service = Mockito.spy(new RacingEventServiceImpl());
        securityService = new SecurityBundleTestWrapper().initializeSecurityServiceForTesting();
        Mockito.doReturn(securityService).when(service).getSecurityService();
        event = service.addEvent("Test", "Test Event", TimePoint.now(), TimePoint.now().plus(Duration.ONE_WEEK), "Here",
                /* isPublic */ true, UUID.randomUUID());
        leaderboardGroup = new LeaderboardGroupImpl("LG", "LGDesc", "The LC", /* displayGroupsInReverseOrder */ false,
                Collections.emptyList());
        overallLeaderboard = new LeaderboardGroupMetaLeaderboard(leaderboardGroup, new LowPoint(),
                new ThresholdBasedResultDiscardingRuleImpl(new int[0]));
        leaderboardGroup.setOverallLeaderboard(overallLeaderboard);
        event.addLeaderboardGroup(leaderboardGroup);
        ThreadContext.unbindSubject(); // ensure that a new subject is created that knows the current security manager
        subject = SecurityUtils.getSubject(); // this also binds the Subject to the ThreadContext
        subject.login(new UsernamePasswordToken("admin", "admin"));
        threadState = new SubjectThreadState(subject);
        
    }

    @Test
    public void testLeagueEventHierarchyOwnershipChange() {
        SailingHierarchyOwnershipUpdater.createOwnershipUpdater(/* createNewGroup */ true , /* existingGroupIdOrNull */ null,
                /* newGroupName */ "The new owning group",
                /* migrateCompetitors */ true, /* migrateBoats */ true, /* copyMembersAndRoles */ true,
                service)
            .updateGroupOwnershipForEventHierarchy(event);
        // if this works without an exception, we're happy; see bug 5541
    }
    
    @After
    public void tearDown() throws UserManagementException {
        threadState.restore();
        subject.logout();
        securityService.deleteUser("admin");
    }
}
