package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsAndBoatsFromRaceLogEvent;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterBoatEvent;
import com.sap.sailing.domain.base.Boat;

/**
 * This class searches for RegisterBoatsEvents in the given log.
 * 
 * TLDR: It's likely, that you shouldn't use this, but you might want to use {@link RegisteredBoatsAnalyzer} or {@link RaceLogRegisteredBoatsAnalyzer}
 * 
 * Note that solemnly analyzing a certain race or RegattaLog my not lead the correct registered boats for the race
 * corresponding to the RaceLog/the races corresponding to the RegattaLog.
 * 
 * This is the case as by default the boats registered on the RegattaLog are used for each race of the regatta
 * ignoring the ones in the RaceLog (if present). The RaceLog may override this behavior with the
 * {@link RaceLogUseCompetitorsAndBoatsFromRaceLogEvent}. Is an event of this type present, the boats written into the
 * RaceLog are used instead of the boats in the RegattaLog.
 *
 */
public class BoatsInLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<Boat>> {

    public BoatsInLogAnalyzer(LogT log) {
        super(log);
    }

    @Override
    protected Set<Boat> performAnalysis() {
        Set<Boat> result = new HashSet<Boat>();

        for (EventT event : getLog().getUnrevokedEvents()) {
            if (event instanceof RegisterBoatEvent) {
                result.add(((RegisterBoatEvent<?>) event).getBoat());
            }
        }

        return result;
    }
}