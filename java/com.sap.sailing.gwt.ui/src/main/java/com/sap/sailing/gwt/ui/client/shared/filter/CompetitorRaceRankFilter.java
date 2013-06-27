package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.filter.AbstractNumberFilter;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * A filter filtering competitors by their race rank
 * 
 * @author Frank
 * 
 */
public class CompetitorRaceRankFilter extends AbstractNumberFilter<CompetitorDTO, Integer> implements
        LeaderboardFilterContext, SelectedRaceFilterContext, FilterWithUI<CompetitorDTO> {
    public static final String FILTER_NAME = "CompetitorRaceRankFilter";

    private LeaderboardFetcher leaderboardFetcher;
    private RaceIdentifier selectedRace;

    public CompetitorRaceRankFilter() {
    }

    private LeaderboardDTO getLeaderboard() {
        return leaderboardFetcher != null ? leaderboardFetcher.getLeaderboard() : null;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        if (value != null && operator != null && getLeaderboard() != null && getSelectedRace() != null) {
            RaceColumnDTO theRaceColumnDTOThatContainsCompetitorRace = null;
            for (RaceColumnDTO raceColumnDTO : getLeaderboard().getRaceList()) {
                if (raceColumnDTO.containsRace(getSelectedRace())) {
                    theRaceColumnDTOThatContainsCompetitorRace = raceColumnDTO;
                    break;
                }
            }
            // There may be competitors that have no tracked race assigned in that column and therefore won't have a fleet;
            // those competitors are to be considered "worse"
            if (theRaceColumnDTOThatContainsCompetitorRace != null && getLeaderboard().rows.get(competitorDTO) != null) {
                List<CompetitorDTO> rankedCompetitors = getLeaderboard().getCompetitorsFromBestToWorst(theRaceColumnDTOThatContainsCompetitorRace);
                if (rankedCompetitors.isEmpty()) {
                    rankedCompetitors = getLeaderboard().competitors;
                }
                LeaderboardEntryDTO entryDTO = getLeaderboard().rows.get(competitorDTO).fieldsByRaceColumnName.get(theRaceColumnDTOThatContainsCompetitorRace.getName());
                int counter = 0; int raceRank = rankedCompetitors.indexOf(competitorDTO)+1;
                for (CompetitorDTO competitorDTOIterated : rankedCompetitors) {
                    LeaderboardEntryDTO entryDTOIterated = getLeaderboard().rows.get(competitorDTOIterated).fieldsByRaceColumnName.get(theRaceColumnDTOThatContainsCompetitorRace.getName());
                    if (entryDTOIterated.fleet == null && entryDTO.fleet == null
                            || entryDTOIterated.fleet != null && entryDTOIterated.fleet.equals(entryDTO.fleet)) {
                        counter += 1;
                        if (competitorDTOIterated.equals(competitorDTO)) {
                            raceRank = counter;
                            break;
                        }
                    }
                }
                
                if (raceRank > 0) {
                    result = operator.matchValues(value, raceRank);
                }
            }
        }
        return result;
    }

    @Override
    public CompetitorRaceRankFilter copy() {
        CompetitorRaceRankFilter result = new CompetitorRaceRankFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.raceRank();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return stringMessages.raceRank();
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if (value != null) {
            Integer intfilterValue = (Integer) value;
            if (intfilterValue <= 0) {
                errorMessage = stringMessages.numberMustBePositive();
            }
        } else {
            errorMessage = stringMessages.pleaseEnterANumber();
        }
        return errorMessage;
    }

    @Override
    public LeaderboardFetcher getLeaderboardFetcher() {
        return leaderboardFetcher;
    }

    @Override
    public void setLeaderboardFetcher(LeaderboardFetcher leaderboardFetcher) {
        this.leaderboardFetcher = leaderboardFetcher;
    }

    @Override
    public RaceIdentifier getSelectedRace() {
        return selectedRace;
    }

    @Override
    public void setSelectedRace(RaceIdentifier selectedRace) {
        this.selectedRace = selectedRace;
    }

    @Override
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return new CompetitorRaceRankFilterUIFactory(this);
    }
}
