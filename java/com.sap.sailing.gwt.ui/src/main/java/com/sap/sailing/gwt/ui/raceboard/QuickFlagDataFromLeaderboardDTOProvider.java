package com.sap.sailing.gwt.ui.raceboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sse.common.Util;

/**
 * Computes flag data maps based on data coming from Leaderboard or delivered asynchronously from server.
 * For Ranks Computes a map of {@link QuickRankDTO}s from a {@link LeaderboardDTO} and a {@link RaceCompetitorSet}, restricting
 * the calculation to those competitors actually taking part in a race. If the {@link LeaderboardDTO} does not contain
 * detail information about the race, only the order "from best to worst" is used, and ranks are determined based on it,
 * whereas the {@link QuickRankDTO#legNumberOneBased leg numbers} will continue to be accepted from the quick ranks
 * coming from the server.
 * For Speeds populates <code>quickSpeeds</code> with <code>currentSpeedOverGroundInKnots</code> values of 
 * {@link LegEntryDTO}  
 * <p>
 * 
 * When the leaderboard DTO is still empty of {@link #leaderboardNotCurrentlyUpdating not currently being updated}, the
 * flag data coming from the server will be accepted. Otherwise, once a {@link LeaderboardDTO} has been received, its
 * flag data information will take precedence over data delivered asynchronously from the server.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class QuickFlagDataFromLeaderboardDTOProvider extends AbstractQuickFlagDataProvider {
    private final Map<String, QuickRankDTO> quickRanks = new LinkedHashMap<>();
    private final Map<CompetitorDTO, Double> quickSpeeds = new LinkedHashMap<>();
    private final RaceCompetitorSet raceCompetitorSet;
    private final RaceIdentifier selectedRace;
    private boolean lastLeaderboardProvidedLegNumbers;
    private String raceColumnName;
    private boolean leaderboardNotCurrentlyUpdating;

    public QuickFlagDataFromLeaderboardDTOProvider(RaceCompetitorSet raceCompetitorSet, RaceIdentifier selectedRace) {
        this.raceCompetitorSet = raceCompetitorSet;
        this.selectedRace = selectedRace;
    }

    public void setLeaderboardNotCurrentlyUpdating(boolean leaderboardNotCurrentlyUpdating) {
        this.leaderboardNotCurrentlyUpdating = leaderboardNotCurrentlyUpdating;
    }

    @Override
    public void quickRanksReceivedFromServer(Map<String, QuickRankDTO> quickRanksFromServer) {
        if (quickRanks.isEmpty() || leaderboardNotCurrentlyUpdating) {
            for (final Entry<String, QuickRankDTO> e : quickRanksFromServer.entrySet()) {
                quickRanks.put(e.getKey(), e.getValue());
                notifyListenersRankChanged(e.getKey(), /* oldQuickRank */ null, e.getValue());
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

    public void updateFlagData(final LeaderboardDTO leaderboard) {
        updateQuickRanks(leaderboard);
        updateQuickSpeeds(leaderboard);
    }

    private void updateQuickRanks(final LeaderboardDTO leaderboard) {
        determineRaceColumnName(leaderboard);
        if (raceColumnName != null) {
            final List<CompetitorDTO> competitorsFromBestToWorst = leaderboard
                    .getCompetitorsFromBestToWorst(raceColumnName);
            if (competitorsFromBestToWorst.isEmpty()) {
                for (CompetitorDTO c : leaderboard.competitors) {
                    final QuickRankDTO quickRank = new QuickRankDTO(c, /* oneBasedRank */ 0, /* leg number ignored */ 0);
                    QuickRankDTO oldQuickRank = quickRanks.put(c.getIdAsString(), quickRank);
                    if (Util.equalsWithNull(oldQuickRank, quickRank)) {
                        notifyListenersRankChanged(c.getIdAsString(), oldQuickRank, quickRank);
                    }
                }
            } else {
                int oneBasedRank = 1;
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
                            final QuickRankDTO quickRankDTO = new QuickRankDTO(c, oneBasedRank, oneBasedLegNumber);
                            quickRanks.put(c.getIdAsString(), quickRankDTO);
                            notifyListenersRankChanged(c.getIdAsString(), /* oldQuickRank */ null, quickRankDTO);
                        } else {
                            final QuickRankDTO oldQuickRank = new QuickRankDTO(quickRankToUpdate.competitor, quickRankToUpdate.oneBasedRank, quickRankToUpdate.legNumberOneBased);
                            quickRankToUpdate.oneBasedRank = oneBasedRank;
                            if (lastLeaderboardProvidedLegNumbers) {
                                quickRankToUpdate.legNumberOneBased = oneBasedLegNumber;
                            }
                            notifyListenersRankChanged(c.getIdAsString(), oldQuickRank, quickRankToUpdate);
                        }
                        oneBasedRank++;
                    }
                }
            }
        }
    }

    @Override
    public Map<String, QuickRankDTO> getQuickRanks() {
        return quickRanks;
    }

    private void updateQuickSpeeds(final LeaderboardDTO leaderboard) {
        determineRaceColumnName(leaderboard);
        if (raceColumnName != null) {
            final List<CompetitorDTO> competitorsFromBestToWorst = leaderboard
                    .getCompetitorsFromBestToWorst(raceColumnName);
            for (final CompetitorDTO competitor : competitorsFromBestToWorst) {
                if (Util.contains(raceCompetitorSet.getIdsOfCompetitorsParticipatingInRaceAsStrings(),
                        competitor.getIdAsString())) {
                    final LeaderboardRowDTO row = leaderboard.rows.get(competitor);
                    Double speed = 0d;
                    if (row != null) {
                        final LeaderboardEntryDTO raceEntryForCompetitor = row.fieldsByRaceColumnName
                                .get(raceColumnName);
                        List<LegEntryDTO> legDetailsList = raceEntryForCompetitor.legDetails;
                        if (legDetailsList != null && !legDetailsList.isEmpty()) {
                            LegEntryDTO legEntryDTO = legDetailsList.get(0);
                            if (legEntryDTO != null) {
                                speed = legEntryDTO.currentSpeedOverGroundInKnots;
                            }
                        }
                    }
                    if (speed != null) {
                        quickSpeeds.put(competitor, speed);
                        notifyListenersSpeedChanged(competitor, speed);
                    }
                }
            }
        }
    }

    private void determineRaceColumnName(final LeaderboardDTO leaderboard) {
        RaceColumnDTO raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null || !raceColumn.containsRace(selectedRace)) {
            raceColumnName = getRaceColumnName(leaderboard, selectedRace);
        }
    }

    @Override
    public void quickSpeedsReceivedFromServer(Map<CompetitorDTO, Double> quickSpeedsFromServer) {
        if (quickSpeeds.isEmpty() || leaderboardNotCurrentlyUpdating) {
            for (final Entry<CompetitorDTO, Double> e : quickSpeedsFromServer.entrySet()) {
                quickSpeeds.put(e.getKey(), e.getValue());
                notifyListenersSpeedChanged(e.getKey(), e.getValue());
            }
        } else {
            for (final Entry<CompetitorDTO, Double> e : quickSpeeds.entrySet()) {
                final Double speed = quickSpeedsFromServer.get(e.getKey());
                if (speed != null) {
                    quickSpeeds.put(e.getKey(), speed);
                }
            }
        }

    }

    @Override
    public Map<CompetitorDTO, Double> getQuickSpeeds() {
        return quickSpeeds;
    }

    private String getRaceColumnName(LeaderboardDTO leaderboard, RaceIdentifier selectedRace) {
        for (final RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            if (raceColumn.containsRace(selectedRace)) {
                return raceColumn.getName();
            }
        }
        return null;
    }
}
