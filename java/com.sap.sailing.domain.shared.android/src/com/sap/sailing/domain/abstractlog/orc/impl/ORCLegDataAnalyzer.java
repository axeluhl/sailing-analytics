package com.sap.sailing.domain.abstractlog.orc.impl;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
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
            if (event instanceof RaceLogORCLegDataEvent) {
                Bearing twa = ((RaceLogORCLegDataEvent) event).getTwa();
                Distance length = ((RaceLogORCLegDataEvent) event).getLength();
                int legNr = ((RaceLogORCLegDataEvent) event).getLegNr();
                return new ORCLegDataAnalyzerResult(length, twa, legNr);
            }
        }
        return null;
    }

}
