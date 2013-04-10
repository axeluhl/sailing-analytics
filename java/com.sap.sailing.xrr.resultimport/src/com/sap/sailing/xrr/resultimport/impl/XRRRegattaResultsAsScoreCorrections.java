package com.sap.sailing.xrr.resultimport.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Event;
import com.sap.sailing.xrr.resultimport.schema.Race;
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
        final List<ScoreCorrectionsForRace> result = new ArrayList<ScoreCorrectionsForRace>();
        final Set<String> raceIDsForWhichWeHaveResults = new LinkedHashSet<>();
        final Map<BigInteger, Set<String>> raceNumberToRaceIDs = new HashMap<>();
        // the mapping from raceID to race number is defined in the <Race> elements in the <Event> element; this
        // is particularly important for split-fleet races where this truly is a n:1 association
        for (Object o : event.getRaceOrDivisionOrRegattaSeriesResult()) {
            if (o instanceof Race) {
                final Race race = (Race) o;
                BigInteger raceNumber = race.getRaceNumber();
                String raceID = race.getRaceID();
                Set<String> raceIDsForRaceNumber = raceNumberToRaceIDs.get(raceNumber);
                if (raceIDsForRaceNumber == null) {
                    raceIDsForRaceNumber = new HashSet<>();
                    raceNumberToRaceIDs.put(raceNumber, raceIDsForRaceNumber);
                }
                raceIDsForRaceNumber.add(raceID);
            }
        }
        for (Object o : division.getSeriesResultOrRaceResultOrTRResult()) {
            // TODO what about TRRaceResult and TRSeriesResult
            if (o instanceof RaceResult) {
                final String raceID = ((RaceResult) o).getRaceID();
                if (!raceIDsForWhichWeHaveResults.contains(raceID)) {
                    raceIDsForWhichWeHaveResults.add(raceID);
                }
            }
        }
        for (Map.Entry<BigInteger, Set<String>> raceNumberAndSetOfRaceIDs : raceNumberToRaceIDs.entrySet()) {
            Set<String> intersectionWithRaceIDsForWhichWeHaveResults = new HashSet<String>(raceNumberAndSetOfRaceIDs.getValue());
            intersectionWithRaceIDsForWhichWeHaveResults.retainAll(raceIDsForWhichWeHaveResults);
            if (!intersectionWithRaceIDsForWhichWeHaveResults.isEmpty()) {
                result.add(new ScoreCorrectionForRaceResult(parser, division, raceNumberAndSetOfRaceIDs.getKey(), raceNumberAndSetOfRaceIDs.getValue()));
            }
        }
        return result;
    }
}
