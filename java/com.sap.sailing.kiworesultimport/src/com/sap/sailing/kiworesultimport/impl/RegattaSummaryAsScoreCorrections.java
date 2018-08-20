package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.BoatResultInRace;
import com.sap.sailing.kiworesultimport.Crewmember;
import com.sap.sailing.kiworesultimport.RaceSummary;
import com.sap.sailing.kiworesultimport.RegattaSummary;

public class RegattaSummaryAsScoreCorrections implements RegattaScoreCorrections {
    private final RegattaSummary regattaSummary;
    private final ScoreCorrectionProvider provider;
    
    public RegattaSummaryAsScoreCorrections(RegattaSummary regattaSummary, ScoreCorrectionProvider provider) {
        super();
        this.regattaSummary = regattaSummary;
        this.provider = provider;
    }

    @Override
    public String getRegattaName() {
        return regattaSummary.getEventName()+" ("+regattaSummary.getBoatClassName()+")";
    }

    @Override
    public ScoreCorrectionProvider getProvider() {
        return provider;
    }

    @Override
    public Iterable<ScoreCorrectionsForRace> getScoreCorrectionsForRaces() {
        List<ScoreCorrectionsForRace> result = new ArrayList<ScoreCorrectionsForRace>();
        for (RaceSummary raceSummary : regattaSummary.getRaces()) {
            result.add(new ZipFileScoreCorrectionsForRace(raceSummary));
        }
        return result;
    }
    
    private class ZipFileScoreCorrectionsForRace implements ScoreCorrectionsForRace {
        private final RaceSummary raceSummary;
        
        public ZipFileScoreCorrectionsForRace(RaceSummary raceSummary) {
            this.raceSummary = raceSummary;
        }

        @Override
        public String getRaceNameOrNumber() {
            return ""+raceSummary.getRaceNumber();
        }

        @Override
        public Set<String> getSailIDs() {
            Set<String> result = new HashSet<String>();
            for (Boat boat : raceSummary.getBoats()) {
                result.add(boat.getSailingNumber());
            }
            return result;
        }

        @Override
        public ScoreCorrectionForCompetitorInRace getScoreCorrectionForCompetitor(String sailID) {
            ScoreCorrectionForCompetitorInRace result = null;
            final Boat boat = raceSummary.getBoat(sailID);
            if (boat != null) {
                String boatName = getNameForBoat(boat);
                result = new ZipFileScoreCorrectionForCompetitorInRace(raceSummary.getBoatResults(boat), sailID, boatName);
            }
            return result;
        }

        private String getNameForBoat(Boat boat) {
            String result = boat.getName(); // often empty
            if (result == null || result.trim().length() == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(boat.getCrew().getSkipper().getName());
                for (Crewmember crewmember : boat.getCrew().getCrewmembers()) {
                    sb.append(" + ");
                    sb.append(crewmember.getName());
                }
                result = sb.toString();
            }
            return result;
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
