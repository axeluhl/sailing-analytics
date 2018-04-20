package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet.CompetitorsForRaceDefinedListener;
import com.sap.sse.common.Util;

public abstract class RaceBoardModeWithPerRaceCompetitors extends AbstractRaceBoardMode implements CompetitorsForRaceDefinedListener {
    private Iterable<CompetitorWithBoatDTO> competitorsInRace;

    abstract protected void updateCompetitorSelection();

    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        super.applyTo(raceBoardPanel);
        raceBoardPanel.getMap().addCompetitorsForRaceDefinedListener(this);
    }

    protected void stopReceivingCompetitorsInRace() {
        getRaceBoardPanel().getMap().removeCompetitorsForRaceDefinedListener(this);
    }

    protected void updateCompetitorSelection(final int howManyTopCompetitorsInRaceToSelect, LeaderboardDTO leaderboard) {
        if (leaderboard != null) {
            final List<CompetitorWithBoatDTO> competitorsFromBestToWorstInColumn = leaderboard.getCompetitorsFromBestToWorst(getRaceColumn());
            final Set<CompetitorWithBoatDTO> competitorsToSelect = new HashSet<>();
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

    @Override
    public void competitorsForRaceDefined(Iterable<CompetitorWithBoatDTO> competitorsInRace) {
        this.competitorsInRace = competitorsInRace;
        trigger();
    }
    
    protected Iterable<CompetitorWithBoatDTO> getCompetitorsInRace() {
        return competitorsInRace;
    }

}
