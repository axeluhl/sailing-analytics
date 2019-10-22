package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.function.Function;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util.Pair;

public class RaceLogORCScratchBoatAnalyzer extends BaseLogAnalyzer<RaceLog, RaceLogEvent, RaceLogEventVisitor, Pair<Competitor, RaceLogORCScratchBoatEvent>> {
    private static final Logger logger = Logger.getLogger(RaceLogORCScratchBoatAnalyzer.class.getName());
    private final Function<Serializable, Competitor> competitorsById;

    public RaceLogORCScratchBoatAnalyzer(RaceLog log, Function<Serializable, Competitor> competitorsById) {
        super(log);
        this.competitorsById = competitorsById;
    }

    @Override
    protected Pair<Competitor, RaceLogORCScratchBoatEvent> performAnalysis() {
        for (final RaceLogEvent o : getLog().getUnrevokedEventsDescending()) {
            if (o instanceof RaceLogORCScratchBoatEvent) {
                RaceLogORCScratchBoatEvent event = (RaceLogORCScratchBoatEvent) o;
                final Serializable competitorId = event.getCompetitorId();
                final Competitor competitor = competitorsById.apply(competitorId);
                if (competitor != null) {
                    return new Pair<>(competitor, event);
                } else {
                    logger.warning("Unable to find competitor with ID " + competitorId
                            + " as an ORC Performance Curve scratch boat. Race log event is ignored.");
                }
            }
        }
        return null;
    }

}
