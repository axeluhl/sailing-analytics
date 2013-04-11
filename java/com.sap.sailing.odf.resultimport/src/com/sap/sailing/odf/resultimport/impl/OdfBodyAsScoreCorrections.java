package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.odf.resultimport.Athlete;
import com.sap.sailing.odf.resultimport.Competition;
import com.sap.sailing.odf.resultimport.CumulativeResult;
import com.sap.sailing.odf.resultimport.OdfBody;

public class OdfBodyAsScoreCorrections implements RegattaScoreCorrections {
    private final OdfBody body;
    private final ScoreCorrectionProvider provider;
    
    public OdfBodyAsScoreCorrections(OdfBody body, ScoreCorrectionProvider provider) {
        super();
        this.body = body;
        this.provider = provider;
    }

    @Override
    public String getRegattaName() {
        return body.getEventName()+" ("+body.getBoatClassName()+")";
    }

    @Override
    public ScoreCorrectionProvider getProvider() {
        return provider;
    }

    @Override
    public Iterable<ScoreCorrectionsForRace> getScoreCorrectionsForRaces() {
        List<ScoreCorrectionsForRace> result = new ArrayList<ScoreCorrectionsForRace>();
        for (Competition competition : body.getCompetitions()) {
            // find out the number of races, including a medal race
            int maxNumberOfRegularRaces = 0;
            int numberOfMedalRaces = 0;
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                final int numberOfRegularRaces = Util.size(cumulativeResult.getPointsAndRanksAfterEachRace());
                maxNumberOfRegularRaces = Math.max(maxNumberOfRegularRaces, numberOfRegularRaces);
                if (cumulativeResult.getPointsInMedalRace() != null) {
                    numberOfMedalRaces = 1;
                }
            }
            for (int i=0; i<maxNumberOfRegularRaces; i++) {
                result.add(new ScoreCorrectionForRegularRace(competition, i));
            }
            for (int i=0; i<numberOfMedalRaces; i++) {
                result.add(new ScoreCorrectionForMedalRace(competition, i));
            }
        }
        return result;
    }
    
    private class ScoreCorrectionForRegularRace implements ScoreCorrectionsForRace {
        private final Competition competition;
        private final int raceNumberStartingWithZero;
        
        public ScoreCorrectionForRegularRace(Competition competition, int raceNumberStartingWithZero) {
            this.competition= competition;
            this.raceNumberStartingWithZero = raceNumberStartingWithZero;
        }

        @Override
        public String getRaceNameOrNumber() {
            return "R"+(raceNumberStartingWithZero+1);
        }

        @Override
        public Set<String> getSailIDs() {
            Set<String> result = new HashSet<>();
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                result.add(cumulativeResult.getCompetitorCode());
            }
            return result;
        }

        @Override
        public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
            ScoreCorrectionForCompetitorInRace result = null;
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                if (sailID.equals(cumulativeResult.getCompetitorCode())) {
                    result = new RegularRaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace(
                            raceNumberStartingWithZero, cumulativeResult, sailID);
                }
            }
            return result;
        }
    }
    
    private class ScoreCorrectionForMedalRace implements ScoreCorrectionsForRace {
        private final Competition competition;
        private final int medalRaceNumberStartingWithZero;
        
        public ScoreCorrectionForMedalRace(Competition competition, int medalRaceNumberStartingWithZero) {
            this.competition = competition;
            this.medalRaceNumberStartingWithZero = medalRaceNumberStartingWithZero;
        }

        @Override
        public String getRaceNameOrNumber() {
            return "M"+(medalRaceNumberStartingWithZero+1);
        }

        @Override
        public Set<String> getSailIDs() {
            Set<String> result = new HashSet<>();
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                if (cumulativeResult.getPointsInMedalRace() != null) {
                    result.add(cumulativeResult.getCompetitorCode());
                }
            }
            return result;
        }

        @Override
        public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
            ScoreCorrectionForCompetitorInRace result = null;
            for (CumulativeResult cumulativeResult : competition.getCumulativeResults()) {
                if (sailID.equals(cumulativeResult.getCompetitorCode())) {
                    result = new MedalRaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace(
                            medalRaceNumberStartingWithZero, cumulativeResult, sailID);
                }
            }
            return result;
        }
    }
    
    private abstract class RaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace implements ScoreCorrectionForCompetitorInRace {
        private final int raceNumberStartingFromZero;
        private final CumulativeResult cumulativeResult;
        private final String competitorCode;

        public RaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace(int raceNumberStartingFromZero,
                CumulativeResult cumulativeResult, String competitorCode) {
            this.raceNumberStartingFromZero = raceNumberStartingFromZero;
            this.cumulativeResult = cumulativeResult;
            this.competitorCode = competitorCode;
        }

        protected CumulativeResult getCumulativeResult() {
            return cumulativeResult;
        }

        @Override
        public String getSailID() {
            return competitorCode;
        }

        @Override
        public String getCompetitorName() {
            StringBuilder result = new StringBuilder();
            for (Iterator<Athlete> i=getCumulativeResult().getAthletes().iterator(); i.hasNext(); ) {
                Athlete athlete = i.next();
                result.append(athlete.getName());
                if (i.hasNext()) {
                    result.append(" + ");
                }
            }
            return result.toString();
        }

        protected int getRaceNumberStartingFromZero() {
            return raceNumberStartingFromZero;
        }
    }

    private class RegularRaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace extends RaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace {
        public RegularRaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace(int raceNumberStartingFromZero,
                CumulativeResult cumulativeResult, String competitorCode) {
            super(raceNumberStartingFromZero, cumulativeResult, competitorCode);
        }

        @Override
        public Double getPoints() {
            return Util.get(getCumulativeResult().getPointsAndRanksAfterEachRace(), getRaceNumberStartingFromZero()).getA();
        }

        @Override
        public MaxPointsReason getMaxPointsReason() {
            return Util.get(getCumulativeResult().getPointsAndRanksAfterEachRace(), getRaceNumberStartingFromZero()).getC();
        }

        @Override
        public Boolean isDiscarded() {
            // we don't know from the format; if there are two discards, it's a bad puzzle to solve; let the leaderboard do it
            return null;
        }
    }

    private class MedalRaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace extends RaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace {
        public MedalRaceFromCumulativeResultAsScoreCorrectionForCompetitorInRace(int raceNumberStartingFromZero,
                CumulativeResult cumulativeResult, String competitorCode) {
            super(raceNumberStartingFromZero, cumulativeResult, competitorCode);
        }

        @Override
        public Double getPoints() {
            return getCumulativeResult().getPointsInMedalRace();
        }

        @Override
        public MaxPointsReason getMaxPointsReason() {
            return MaxPointsReason.NONE;
        }

        /**
         * We assume that a medal race cannot be discarded
         */
        @Override
        public Boolean isDiscarded() {
            return false;
        }
    }
}
