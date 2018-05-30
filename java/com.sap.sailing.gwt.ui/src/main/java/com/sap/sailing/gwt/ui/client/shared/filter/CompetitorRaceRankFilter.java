package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.LinkedHashSet;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.AbstractNumberFilter;

/**
 * A filter filtering competitors by their net points in a race
 * 
 * @author Frank
 * 
 */
public class CompetitorRaceRankFilter extends AbstractNumberFilter<CompetitorDTO, Integer> implements
        LeaderboardFilterContext, SelectedRaceFilterContext, FilterWithUI<CompetitorDTO> {
    public static final String FILTER_NAME = "Race Rank";

    private LeaderboardFetcher leaderboardFetcher;
    private RaceIdentifier selectedRace;
    
    /**
     * An optional quick rank provider; if <code>null</code>, a more expensive algorithm based on the {@link #selectedRace}
     * and the leaderboard that can be obtained through the {@link #leaderboardFetcher} needs to be employed to obtain the rank.
     */
    private QuickRankProvider quickRankProvider;

    public CompetitorRaceRankFilter() {
    }

    private LeaderboardDTO getLeaderboard() {
        return leaderboardFetcher != null ? leaderboardFetcher.getLeaderboard() : null;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        final boolean result;
        Integer raceRank = null;
        if (value != null && operator != null) {
            if (quickRankProvider != null) {
                // obtain the quick rank
                raceRank = quickRankProvider.getRank(competitorDTO);
            } else {
                if (getLeaderboard() != null && getSelectedRace() != null) {
                    raceRank = getRankFromLeaderboard(competitorDTO);
                }
            }
        }
        if (raceRank != null) {
            result = operator.matchValues(value, raceRank);
        } else {
            result = false;
        }
        return result;
    }

    /**
     * @return <code>null</code> if no rank could be determined for <code>competitorDTO</code>, a 1-based rank otherwise
     */
    private Integer getRankFromLeaderboard(CompetitorDTO competitorDTO) {
        Integer raceRank = null;
        RaceColumnDTO theRaceColumnDTOThatContainsCompetitorRace = null;
        for (RaceColumnDTO raceColumnDTO : getLeaderboard().getRaceList()) {
            if (raceColumnDTO.containsRace(getSelectedRace())) {
                theRaceColumnDTOThatContainsCompetitorRace = raceColumnDTO;
                break;
            }
        }
        // There may be competitors that have no tracked race assigned in that column and therefore won't
        // have a fleet;
        // those competitors are to be considered "worse". However, in one column it may also be the case
        // that the "selected race"
        // has no scores yet (e.g., not yet started) and that other races in the column do have a score. The
        // competitorsFromBestToWorst then would not contain those competitors that don't have a score
        // assigned yet.
        final LeaderboardRowDTO competitorRow = getLeaderboard().rows.get(competitorDTO);
        LinkedHashSet<CompetitorDTO> competitorsRankedInColumn = new LinkedHashSet<>();
        if (theRaceColumnDTOThatContainsCompetitorRace != null && competitorRow != null) {
            LeaderboardEntryDTO entryDTO = competitorRow.fieldsByRaceColumnName
                    .get(theRaceColumnDTOThatContainsCompetitorRace.getName());

            // first collect those competitors in their order that have a rank
            for (CompetitorDTO competitor : getLeaderboard().getCompetitorsFromBestToWorst(
                    theRaceColumnDTOThatContainsCompetitorRace)) {
                competitorsRankedInColumn.add(competitor);
            }
            // then add all competitors for which no ranking has been provided
            for (CompetitorDTO competitor : getLeaderboard().competitors) {
                if (!competitorsRankedInColumn.contains(competitor)) {
                    competitorsRankedInColumn.add(competitor);
                }
            }
            raceRank = 0;
            for (CompetitorDTO competitor : competitorsRankedInColumn) {
                LeaderboardEntryDTO entryDTOIterated = getLeaderboard().rows.get(competitor).fieldsByRaceColumnName
                        .get(theRaceColumnDTOThatContainsCompetitorRace.getName());
                // the competitor counts for the selected race if the fleet matches or is unknown
                if (entryDTOIterated.fleet == null || entryDTO.fleet == null
                        || entryDTOIterated.fleet.equals(entryDTO.fleet)) {
                    raceRank++;
                }
                if (competitor.equals(competitorDTO)) {
                    break;
                }
            }
        }
        return raceRank;
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
        if (this.getOperator() != null) {
            return FILTER_NAME + " " + this.getOperator().getName() + " " + this.getValue();
        }
        return FILTER_NAME + " " + this.getValue();
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.raceRank();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return "Top " + this.getValue() + " " + stringMessages.raceRank();
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
    public void setQuickRankProvider(QuickRankProvider quickRankProvider) {
        this.quickRankProvider = quickRankProvider;
    }

    @Override
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return new CompetitorRaceRankFilterUIFactory(this);
    }
}
