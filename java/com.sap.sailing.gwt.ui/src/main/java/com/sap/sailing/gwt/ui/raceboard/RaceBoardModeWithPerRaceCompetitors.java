package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sse.common.Util;

public abstract class RaceBoardModeWithPerRaceCompetitors extends AbstractRaceBoardMode {
    private Iterable<CompetitorDTO> competitorsInRace;
    private LeaderboardDTO leaderboard;
    private RaceColumnDTO raceColumn;

    abstract protected void updateCompetitorSelection();

    @Override
    public void competitorsForRaceDefined(Iterable<CompetitorDTO> competitorsInRace) {
        this.competitorsInRace = competitorsInRace;
        updateCompetitorSelection();
    }

    protected Iterable<CompetitorDTO> getCompetitorsInRace() {
        return competitorsInRace;
    }

    protected LeaderboardDTO getLeaderboard() {
        return leaderboard;
    }

    protected void setLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        super.updatedLeaderboard(leaderboard);
        setLeaderboard(leaderboard);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        this.raceColumn = raceColumn;
    }

    protected RaceColumnDTO getRaceColumn() {
        return raceColumn;
    }

    protected void updateCompetitorSelection(final int howManyTopCompetitorsInRaceToSelect) {
        final List<CompetitorDTO> competitorsFromBestToWorstInColumn = getLeaderboard().getCompetitorsFromBestToWorst(getRaceColumn());
        final Set<CompetitorDTO> competitorsToSelect = new HashSet<>();
        int numberOfSelectedCompetitors = 0;
        for (int i=0; numberOfSelectedCompetitors<howManyTopCompetitorsInRaceToSelect && i<competitorsFromBestToWorstInColumn.size(); i++) {
            if (getCompetitorsInRace() == null || Util.contains(getCompetitorsInRace(), competitorsFromBestToWorstInColumn.get(i))) {
                competitorsToSelect.add(competitorsFromBestToWorstInColumn.get(i));
                numberOfSelectedCompetitors++;
            }
        }
        getRaceBoardPanel().getCompetitorSelectionProvider().setSelection(competitorsToSelect);
    }

}
