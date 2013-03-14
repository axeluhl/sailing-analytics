package com.sap.sailing.xrr.resultimport.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Event;
import com.sap.sailing.xrr.resultimport.schema.RaceResult;

public class XRRRegattaResultsAsScoreCorrections implements RegattaScoreCorrections {
    private final Event event;
    private final Division division;
    private final Parser parser;
    private final ScoreCorrectionProvider provider;
    
    public XRRRegattaResultsAsScoreCorrections(Event event, Division division, ScoreCorrectionProvider provider,
            Parser parser) {
        super();
        this.event = event;
        this.division = division;
        this.provider = provider;
        this.parser = parser;
    }

    @Override
    public String getRegattaName() {
        return event.getTitle()+" ("+parser.getBoatClassName(division)+")";
    }

    @Override
    public ScoreCorrectionProvider getProvider() {
        return provider;
    }

    @Override
    public Iterable<ScoreCorrectionsForRace> getScoreCorrectionsForRaces() {
        List<ScoreCorrectionsForRace> result = new ArrayList<ScoreCorrectionsForRace>();
        Set<String> raceIDs = new LinkedHashSet<>();
        for (Object o : division.getSeriesResultOrRaceResultOrTRRaceResult()) {
            // TODO what about TRRaceResult and TRSeriesResult
            if (o instanceof RaceResult) {
                final String raceID = ((RaceResult) o).getRaceID();
                if (!raceIDs.contains(raceID)) {
                    raceIDs.add(raceID);
                    result.add(new ScoreCorrectionForRaceResult(parser, division, raceID));
                }
            }
        }
        return result;
    }
}
