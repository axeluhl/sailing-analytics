package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;

/**
 * This class searches for {@link RaceLogRegisterCompetitorEvent}s or {@link RegattaLogRegisterCompetitorEvent}s in the given log
 * in order to determine the competitor/boat mappings for both kind of events
 * 
 */
public class CompetitorsAndBoatsInLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Map<Competitor, Boat>> {

    public CompetitorsAndBoatsInLogAnalyzer(LogT log) {
        super(log);
    }

    @Override
    protected Map<Competitor, Boat> performAnalysis() {
        Map<Competitor, Boat> result = new HashMap<>();
        LogT log = getLog();

        if (log instanceof RegattaLog) {
            for (EventT event : getLog().getUnrevokedEvents()) {
                if (event instanceof RegattaLogRegisterCompetitorEvent) {
                    RegattaLogRegisterCompetitorEvent regattaLogCompetitorEvent = (RegattaLogRegisterCompetitorEvent) event;
                    if (regattaLogCompetitorEvent.getCompetitor().hasBoat()) {
                        CompetitorWithBoat competitorWithBoat = (CompetitorWithBoat) regattaLogCompetitorEvent.getCompetitor();
                        result.put(competitorWithBoat, competitorWithBoat.getBoat());
                    }
                }
            }
        } else if (log instanceof RaceLog) {
            for (EventT event : getLog().getUnrevokedEvents()) {
                if (event instanceof RaceLogRegisterCompetitorEvent) {
                    RaceLogRegisterCompetitorEvent raceLogCompetitorEvent = (RaceLogRegisterCompetitorEvent) event;
                    result.put(raceLogCompetitorEvent.getCompetitor(), raceLogCompetitorEvent.getBoat());
                }
            }
        }

        return result;
    }
}