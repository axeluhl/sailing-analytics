package com.sap.sailing.server.test;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.util.RaceBoardLinkFactory;

public class TrailingSlashInEventBaseNameTest {
    @Test
    public void testTrailingSlashInEventBaseNameCausesNoHarmInRaceBoardLinks() throws MalformedURLException {
        final TrackedRace trackedRace = mock(TrackedRace.class);
        when(trackedRace.getRaceIdentifier()).thenReturn(new RegattaNameAndRaceName("Regatta", "Race"));
        final Leaderboard leaderboard = mock(Leaderboard.class);
        when(leaderboard.getName()).thenReturn("Leaderboard");
        final Event event = mock(Event.class);
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getBaseURL()).thenReturn(new URL("https://myevent.example.com/"));
        final LeaderboardGroup leaderboardGroup = mock(LeaderboardGroup.class);
        when(leaderboardGroup.getId()).thenReturn(UUID.randomUUID());
        final String raceboardMode = "PLAYER";
        final String link = RaceBoardLinkFactory.createRaceBoardLink(trackedRace, leaderboard, event, leaderboardGroup, raceboardMode, Locale.US);
        assertFalse(link.contains("//"));
    }
}
