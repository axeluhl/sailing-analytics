package com.sap.sailing.aiagent.impl.rules;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.json.simple.parser.ParseException;

import com.sap.sailing.aiagent.impl.AIAgentImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

/**
 * After the start mark passings of at least half the number of competitors of the {@link #getTrackedRace() tracked race}
 * have been received, a comment is requested on the start of the best starter.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class GoodStartRule extends Rule {
    public GoodStartRule(AIAgentImpl aiAgent, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet,
            TrackedRace trackedRace) {
        super(aiAgent, leaderboard, raceColumn, fleet, trackedRace);
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        final Iterable<MarkPassing> startMarkPassings = getTrackedRace().getMarkPassingsInOrder(getTrackedRace().getRace().getCourse().getFirstWaypoint());
        if (Util.size(startMarkPassings) >= Util.size(getTrackedRace().getRace().getCompetitors()) / 2) {
            final MarkPassing firstStart = startMarkPassings.iterator().next();
            try {
                final StringBuilder promptBuilder = new StringBuilder();
                promptBuilder
                    .append("Describe, very consisely, the fact that ");
                appendCompetitorToPromptBuilder(firstStart.getCompetitor(), promptBuilder)
                    .append(" started first in race "+getTrackedRace().getRace().getName())
                    .append(". Consider the competitor's start delays in comparison to all other competitors' start delays in previous races of this regatta ")
                    .append(getLeaderboard().getName())
                    .append(" in class "+getLeaderboard().getBoatClass().getName())
                    .append(" that are given below in table form:\n");
                for (final RaceColumn previousRaceColumn : getLeaderboard().getRaceColumns()) {
                    if (previousRaceColumn == getRaceColumn()) {
                        break;
                    }
                    final TrackedRace previousTrackedRace = previousRaceColumn.getTrackedRace(firstStart.getCompetitor());
                    promptBuilder
                        .append("\nStart delays in race ")
                        .append(previousRaceColumn.getName())
                        .append(":\n");
                    for (final Competitor previousRaceCompetitor : previousTrackedRace.getRace().getCompetitors()) {
                        final MarkPassing previousRaceCompetitorStartMarkPassing = previousTrackedRace.getMarkPassing(previousRaceCompetitor, getTrackedRace().getRace().getCourse().getFirstWaypoint());
                        if (previousRaceCompetitorStartMarkPassing != null) {
                            promptBuilder.append("  ");
                            appendCompetitorToPromptBuilder(previousRaceCompetitor, promptBuilder)
                                .append(": ")
                                .append(getTrackedRace().getStartOfRace().until(previousRaceCompetitorStartMarkPassing.getTimePoint()))
                                .append("\n");
                        }
                    }
                }
                promptBuilder
                    .append("Also, consider in your concise comment the typical start performance of this competitor in previous regattas at other events.");
                produceComment(promptBuilder.toString(), firstStart.getTimePoint());
            } catch (UnsupportedOperationException | URISyntaxException | IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
