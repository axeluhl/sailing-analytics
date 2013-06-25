package com.sap.sailing.gwt.ui.client.shared.filter;

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
            RaceColumnDTO theRaceColumnDTO = null;
            for (RaceColumnDTO raceColumnDTO : getLeaderboard().getRaceList()) {
                if (raceColumnDTO.containsRace(getSelectedRace())) {
                    theRaceColumnDTO = raceColumnDTO;
                    break;
                }
            }
            if (theRaceColumnDTO != null && getLeaderboard().rows.get(competitorDTO) != null) {
                LeaderboardEntryDTO entryDTO = getLeaderboard().rows.get(competitorDTO).fieldsByRaceColumnName.get(theRaceColumnDTO.getName());
                if (entryDTO.totalPoints != null || !theRaceColumnDTO.isLiveInServerTime(entryDTO.fleet)) {
                    int raceRank = getLeaderboard().getCompetitorsFromBestToWorst(theRaceColumnDTO).indexOf(competitorDTO)+1;
                    if (raceRank > 0) {
                        result = operator.matchValues(value, raceRank);
                    }
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
