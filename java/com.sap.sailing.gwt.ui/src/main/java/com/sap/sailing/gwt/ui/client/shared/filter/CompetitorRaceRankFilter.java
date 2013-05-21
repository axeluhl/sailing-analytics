package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.filter.AbstractNumberFilter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

/**
 * A filter filtering competitors by their race rank
 * @author Frank
 *
 */
public class CompetitorRaceRankFilter extends AbstractNumberFilter<CompetitorDTO, Integer>
    implements LeaderboardFilterContext, SelectedRaceFilterContext, FilterWithUI<CompetitorDTO> { 
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
            String raceColumnName = null;
            for(RaceColumnDTO raceColumnDTO: getLeaderboard().getRaceList()) {
                if(raceColumnDTO.containsRace(getSelectedRace())) {
                    raceColumnName = raceColumnDTO.name;
                    break;
                }
            }
            if(raceColumnName != null) {
                LeaderboardRowDTO leaderboardRowDTO = getLeaderboard().rows.get(competitorDTO);
                LeaderboardEntryDTO leaderboardEntryDTO = leaderboardRowDTO.fieldsByRaceColumnName.get(raceColumnName);
                if(leaderboardEntryDTO.rank != null) {
                    int raceRank = leaderboardEntryDTO.rank;
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
        if(value != null) {
            Integer intfilterValue = (Integer) value;
            if(intfilterValue <= 0) {
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
