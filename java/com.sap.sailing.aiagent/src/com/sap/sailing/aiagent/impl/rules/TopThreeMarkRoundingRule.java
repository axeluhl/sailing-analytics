package com.sap.sailing.aiagent.impl.rules;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.simple.parser.ParseException;

import com.sap.sailing.aiagent.impl.AIAgentImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tagging.RaceLogNotFoundException;
import com.sap.sailing.domain.common.tagging.ServiceNotFoundException;
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
public class TopThreeMarkRoundingRule extends Rule {
    private final static String TOPIC = "Leg Winners";
    
    private final ConcurrentMap<Waypoint, Boolean> hasFired;
    
    public TopThreeMarkRoundingRule(AIAgentImpl aiAgent, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet,
            TrackedRace trackedRace) {
        super(aiAgent, leaderboard, raceColumn, fleet, trackedRace);
        this.hasFired = new ConcurrentHashMap<>();
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
            Iterable<MarkPassing> markPassings) {
        for (final MarkPassing markPassing : markPassings) {
            // TODO record the first three mark passings for each waypoint (or fewer if there are fewer competitors in the race) and fire when the third has been found and the rule hasn't fired for that waypoint yet...
            if (!hasFired.get(markPassing.getWaypoint()) == Boolean.TRUE) {
                final Iterable<MarkPassing> startMarkPassings = getTrackedRace().getMarkPassingsInOrder(getTrackedRace().getRace().getCourse().getFirstWaypoint());
                if (Util.size(startMarkPassings) >= Util.size(getTrackedRace().getRace().getCompetitors()) / 2) {
                    hasFired.put(markPassing.getWaypoint(), Boolean.TRUE);
                    // TODO
                    final StringBuilder promptBuilder = new StringBuilder();
                    promptBuilder
                        .append("Describe, very consisely, the fact that "); // TODO ...a rounded waypoint x in first, b in second, and c in third position;
                    // TODO talk about gap changes and the gap to the followers in the field (if any) and how they got there; on which course side did they sail?
                    // TODO was there a wind shift that supported the gap / rank changes?
//                        appendCompetitorToPromptBuilder(firstStart.getCompetitor(), promptBuilder)
//                            .append(" started first in race "+getTrackedRace().getRace().getName())
//                            .append(". Consider the competitor's start delays in comparison to all other competitors' start delays in previous races of this regatta ")
//                            .append(getLeaderboard().getName())
//                            .append(" in class "+getLeaderboard().getBoatClass().getName())
//                            .append(" that are given below in table form:\n");
                    for (final RaceColumn previousRaceColumn : getLeaderboard().getRaceColumns()) {
                        if (previousRaceColumn == getRaceColumn()) {
                            break;
                        }
                    }
                    promptBuilder
                        .append("Also, consider in your concise comment the typical start performance of this competitor in previous regattas at other events.");
                    // TODO build the prompt
//                        produceComment(TOPIC, promptBuilder.toString(), firstStart.getTimePoint(),
//                                getClass().getName()); // there is only one start analysis per race, so the class name is sufficient for identification
                }
            }
        }
    }
}
