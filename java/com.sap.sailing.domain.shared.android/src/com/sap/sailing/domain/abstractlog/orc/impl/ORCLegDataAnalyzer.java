package com.sap.sailing.domain.abstractlog.orc.impl;

import com.sap.sailing.domain.abstractlog.orc.ORCLegDataEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

public class ORCLegDataAnalyzer extends RaceLogAnalyzer<ORCLegDataAnalyzerResult> {

    public ORCLegDataAnalyzer(RaceLog raceLog) {
        super(raceLog);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected ORCLegDataAnalyzerResult performAnalysis() {
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof ORCLegDataEvent) {
                Bearing twa = ((ORCLegDataEvent) event).getTwa();
                Distance length = ((ORCLegDataEvent) event).getLength();
                int legNr = ((ORCLegDataEvent) event).getLegNr();
                return new ORCLegDataAnalyzerResult(length, twa, legNr);
            }
        }
        return null;
    }

}
