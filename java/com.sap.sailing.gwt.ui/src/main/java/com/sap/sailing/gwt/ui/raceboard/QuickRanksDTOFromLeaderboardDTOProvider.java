package com.sap.sailing.gwt.ui.raceboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.QuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sse.common.Util;

/**
 * Computes a map of {@link QuickRankDTO}s from a {@link LeaderboardDTO} and a {@link RaceCompetitorSet}, restricting
 * the calculation to those competitors actually taking part in a race. If the {@link LeaderboardDTO} does not contain
 * detail information about the race, only the order "from best to worst" is used, and ranks are determined based on it,
 * whereas the {@link QuickRankDTO#legNumberOneBased leg numbers} will continue to be accepted from the quick ranks
 * coming from the server.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class QuickRanksDTOFromLeaderboardDTOProvider implements QuickRanksDTOProvider {
    private LinkedHashMap<String, QuickRankDTO> quickRanks;
    private final RaceCompetitorSet raceCompetitorSet;
    private final RaceIdentifier selectedRace;
    private boolean lastLeaderboardProvidedLegNumbers;
    private String raceColumnName;
    
    public QuickRanksDTOFromLeaderboardDTOProvider(RaceCompetitorSet raceCompetitorSet, RaceIdentifier selectedRace) {
        this.raceCompetitorSet = raceCompetitorSet;
        this.selectedRace = selectedRace;
    }

    @Override
    public void quickRanksReceivedFromServer(LinkedHashMap<String, QuickRankDTO> quickRanksFromServer) {
        if (quickRanks == null) {
            quickRanks = new LinkedHashMap<>();
            for (final Entry<String, QuickRankDTO> e : quickRanksFromServer.entrySet()) {
                quickRanks.put(e.getKey(), e.getValue());
            }
        } else if (!lastLeaderboardProvidedLegNumbers) {
            // extract at least the leg numbers and update existing quick ranks accordingly in place
            for (final Entry<String, QuickRankDTO> e : quickRanks.entrySet()) {
                final QuickRankDTO quickRankFromServer = quickRanksFromServer.get(e.getKey());
                if (quickRankFromServer != null) {
                    e.getValue().legNumberOneBased = quickRankFromServer.legNumberOneBased;
                }
            }
        }
    }
    
    public void updateQuickRanks(final LeaderboardDTO leaderboard) {
        if (quickRanks == null) {
            quickRanks = new LinkedHashMap<>();
        }
        RaceColumnDTO raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null || !raceColumn.containsRace(selectedRace)) {
            raceColumnName = getRaceColumnName(leaderboard, selectedRace);
        }
        if (raceColumnName != null) {
            final List<CompetitorDTO> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(raceColumnName);
            int rank = 1;
            for (final CompetitorDTO c : competitorsFromBestToWorst) {
                if (Util.contains(raceCompetitorSet.getIdsOfCompetitorsParticipatingInRaceAsStrings(), c.getIdAsString())) {
                    final LeaderboardRowDTO row = leaderboard.rows.get(c);
                    final int oneBasedLegNumber;
                    if (row != null) {
                        final LeaderboardEntryDTO raceEntryForCompetitor = row.fieldsByRaceColumnName.get(raceColumnName);
                        if (raceEntryForCompetitor != null && raceEntryForCompetitor.legDetails != null) {
                            oneBasedLegNumber = raceEntryForCompetitor.getOneBasedCurrentLegNumber();
                            lastLeaderboardProvidedLegNumbers = true;
                        } else {
                            oneBasedLegNumber = 0;
                            lastLeaderboardProvidedLegNumbers = false;
                        }
                    } else {
                        oneBasedLegNumber = 0;
                    }
                    QuickRankDTO quickRankToUpdate = quickRanks.get(c.getIdAsString());
                    if (quickRankToUpdate == null) {
                        quickRanks.put(c.getIdAsString(), new QuickRankDTO(c, rank, oneBasedLegNumber));
                    } else {
                        quickRankToUpdate.rank = rank;
                        if (lastLeaderboardProvidedLegNumbers) {
                            quickRankToUpdate.legNumberOneBased = oneBasedLegNumber;
                        }
                    }
                    rank++;
                }
            }
        }
    }

    private String getRaceColumnName(LeaderboardDTO leaderboard, RaceIdentifier selectedRace) {
        for (final RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            if (raceColumn.containsRace(selectedRace)) {
                return raceColumn.getName();
            }
        }
        return null;
    }

    @Override
    public LinkedHashMap<String, QuickRankDTO> getQuickRanks() {
        return quickRanks;
    }

}
