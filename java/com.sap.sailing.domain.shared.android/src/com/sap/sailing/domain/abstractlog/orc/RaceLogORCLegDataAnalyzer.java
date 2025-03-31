package com.sap.sailing.domain.abstractlog.orc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sse.common.Distance;

/**
 * Returns a map with unique entries per {@link RaceLogORCLegDataEvent#getOneBasedLegNumber() one-based leg number}
 * where the relevant valid leg data is converted to the {@link ORCPerformanceCurveLeg} data. Note that for legs for
 * which no entry is found no adapter to any tracked race or tracked leg is created implicitly. Only entries with
 * explicit race log events are returned.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceLogORCLegDataAnalyzer extends RaceLogAnalyzer<Map<Integer, ORCPerformanceCurveLeg>> {

    public RaceLogORCLegDataAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Map<Integer, ORCPerformanceCurveLeg> performAnalysis() {
        final Map<Integer, ORCPerformanceCurveLeg> result = new HashMap<>();
        for (Entry<Integer, RaceLogORCLegDataEvent> entry : new RaceLogORCLegDataEventFinder(getLog()).analyze().entrySet()) {
            assert entry.getKey() == entry.getValue().getOneBasedLegNumber();
            final RaceLogORCLegDataEvent legDataEvent = entry.getValue();
            final ORCPerformanceCurveLeg leg = createORCPerformanceCurveLeg(legDataEvent);
            result.put(legDataEvent.getOneBasedLegNumber(), leg);
        }
        return result;
    }

    public static ORCPerformanceCurveLeg createORCPerformanceCurveLeg(final RaceLogORCLegDataEvent legDataEvent) {
        final Distance length = legDataEvent.getLength();
        final ORCPerformanceCurveLegTypes type = legDataEvent.getType();
        final ORCPerformanceCurveLeg leg;
        if (type == ORCPerformanceCurveLegTypes.TWA) {
            leg = new ORCPerformanceCurveLegImpl(length, legDataEvent.getTwa());
        } else {
            leg = new ORCPerformanceCurveLegImpl(length, legDataEvent.getType());
        }
        return leg;
    }
}
