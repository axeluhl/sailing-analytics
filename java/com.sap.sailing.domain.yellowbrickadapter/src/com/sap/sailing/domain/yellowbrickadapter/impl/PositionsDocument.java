package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class PositionsDocument {
    private final String raceUrl;
    private final Iterable<TeamPositions> teams;

    public PositionsDocument(String raceUrl, Iterable<TeamPositions> teams) {
        super();
        this.raceUrl = raceUrl;
        this.teams = teams;
    }

    public String getRaceUrl() {
        return raceUrl;
    }

    public Iterable<TeamPositions> getTeams() {
        return teams;
    }
    
    public int getNumberOfFixes() {
        return Util.stream(teams).mapToInt(team->Util.size(team.getPositions())).sum();
    }
    
    /**
     * From all positions in the document returns the latest time stamp, or {@code null} if no position fix exists in this document
     */
    public TimePoint getTimePointOfLastFix() {
        final Stream<TeamPositions> teams = StreamSupport.stream(getTeams().spliterator(), /* parallel */ false);
        final Function<TeamPositions, Stream<TeamPosition>> mapper = tp->StreamSupport.stream(tp.getPositions().spliterator(), /* parallel */ true);
        return teams.flatMap(mapper).map(p->p.getTimePoint()).max(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public String toString() {
        return "PositionsDocument [raceUrl=" + raceUrl + ", teams=" + teams + "]";
    }
}
