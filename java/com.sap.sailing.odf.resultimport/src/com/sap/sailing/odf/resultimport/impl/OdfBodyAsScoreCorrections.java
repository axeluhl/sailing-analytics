package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.odf.resultimport.BoatResultInRace;
import com.sap.sailing.odf.resultimport.Competition;
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
            result.add(new ZipFileScoreCorrectionsForRace(competition));
        }
        return result;
    }
    
    private class ZipFileScoreCorrectionsForRace implements ScoreCorrectionsForRace {
        private final Competition competition;
        
        public ZipFileScoreCorrectionsForRace(Competition competition) {
            this.competition = competition;
        }

        @Override
        public String getRaceNameOrNumber() {
            return null; // TODO
        }

        @Override
        public Set<String> getSailIDs() {
            return null; // TODO
        }

        @Override
        public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
            return null; // TODO
        }
    }
    
    private class ZipFileScoreCorrectionForCompetitorInRace implements ScoreCorrectionForCompetitorInRace {

        private final BoatResultInRace boatResults;
        private final String sailID;
        private final String boatName;

        public ZipFileScoreCorrectionForCompetitorInRace(BoatResultInRace boatResults, String sailID, String boatName) {
            this.boatResults = boatResults;
            this.sailID = sailID;
            this.boatName = boatName;
        }

        @Override
        public String getSailID() {
            return sailID;
        }

        @Override
        public String getCompetitorName() {
            return boatName;
        }

        @Override
        public Double getPoints() {
            return boatResults.getPoints();
        }

        @Override
        public MaxPointsReason getMaxPointsReason() {
            return boatResults.getMaxPointsReason();
        }

        @Override
        public Boolean isDiscarded() {
            return boatResults.isDiscarded();
        }
    }
}
