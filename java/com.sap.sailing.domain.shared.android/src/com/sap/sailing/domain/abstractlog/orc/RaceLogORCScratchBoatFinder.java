package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;

public class RaceLogORCScratchBoatFinder extends BaseLogAnalyzer<RaceLog, RaceLogEvent, RaceLogEventVisitor, Competitor> {
    private static final Logger logger = Logger.getLogger(RaceLogORCScratchBoatFinder.class.getName());
    private final Map<Serializable, Competitor> competitorsById;

    public RaceLogORCScratchBoatFinder(RaceLog log, Map<Serializable, Competitor> competitorsById) {
        super(log);
        this.competitorsById = competitorsById;
    }

    @Override
    protected Competitor performAnalysis() {
        for (final RaceLogEvent o : getLog().getUnrevokedEventsDescending()) {
            if (o instanceof RaceLogORCScratchBoatEvent) {
                RaceLogORCScratchBoatEvent event = (RaceLogORCScratchBoatEvent) o;
                final Serializable competitorId = event.getCompetitorId();
                final Competitor competitor = competitorsById.get(competitorId);
                if (competitor != null) {
                    return competitor;
                } else {
                    logger.warning("Unable to find competitor with ID " + competitorId
                            + " as an ORC Performance Curve scratch boat. Race log event is ignored.");
                }
            }
        }
        return null;
    }

}
