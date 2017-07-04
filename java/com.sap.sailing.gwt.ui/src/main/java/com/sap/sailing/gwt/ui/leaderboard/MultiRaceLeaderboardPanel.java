package com.sap.sailing.gwt.ui.leaderboard;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorRaceRankFilter;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class MultiRaceLeaderboardPanel extends LeaderboardPanel {

    public MultiRaceLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings,
            boolean isEmbedded, RegattaAndRaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            boolean showRaceDetails) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace,
                competitorSelectionProvider, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                showRaceDetails);
        updateSettings(settings);
    }

    public MultiRaceLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings,
            boolean isEmbedded, RegattaAndRaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages, boolean showRaceDetails,
            CompetitorFilterPanel competitorSearchTextBox, boolean showSelectionCheckbox,
            RaceTimesInfoProvider optionalRaceTimesInfoProvider, boolean autoExpandLastRaceColumn,
            boolean adjustTimerDelay, boolean autoApplyTopNFilter, boolean showCompetitorFilterStatus,
            boolean enableSyncScroller) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace,
                competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, showRaceDetails, competitorSearchTextBox, showSelectionCheckbox,
                optionalRaceTimesInfoProvider, autoExpandLastRaceColumn, adjustTimerDelay, autoApplyTopNFilter,
                showCompetitorFilterStatus, enableSyncScroller);
        updateSettings(settings);
    }

    public MultiRaceLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings,
            CompetitorSelectionProvider competitorSelectionProvider, String leaderboardName,
            ErrorReporter errorReporter, StringMessages stringMessages, boolean showRaceDetails) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, competitorSelectionProvider,
                leaderboardName, errorReporter, stringMessages, showRaceDetails);
        updateSettings(settings);
    }

    @Override
    public LeaderboardSettings getSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RaceColumnSelection getDefaultRaceColumnSelection() {
        return new ExplicitRaceColumnSelection();
    }

    @Override
    public String getCompetitorColor(CompetitorDTO competitor) {
        // not used for multi
        return null;
    }

    @Override
    public boolean renderBoatColorIfNecessary(CompetitorDTO competitor, SafeHtmlBuilder sb) {
        return false;
    }

    @Override
    public int getLegCount(LeaderboardDTO leaderboardDTO, String raceColumnName) {
        return leaderboardDTO.getLegCount(raceColumnName, null);
    }

    @Override
    protected LeaderboardSettings overrideDefaultsForNamesOfRaceColumns(LeaderboardSettings currentSettings,
            LeaderboardDTO result) {
        return currentSettings.overrideDefaultsForNamesOfRaceColumns(result.getNamesOfRaceColumns());
    }

    @Override
    protected void applyTop30FilterIfCompetitorSizeGreaterEqual40(LeaderboardDTO leaderboard) {
        int maxRaceRank = 30;
        if (leaderboard.competitors.size() >= 40) {
            CompetitorRaceRankFilter raceRankFilter = new CompetitorRaceRankFilter();
            raceRankFilter.setLeaderboardFetcher(this);
            raceRankFilter.setQuickRankProvider(this.competitorFilterPanel.getQuickRankProvider());
            raceRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
            raceRankFilter.setValue(maxRaceRank);
            FilterSet<CompetitorDTO, Filter<CompetitorDTO>> activeFilterSet = competitorSelectionProvider
                    .getOrCreateCompetitorsFilterSet(stringMessages.topNCompetitorsByRaceRank(maxRaceRank));
            activeFilterSet.addFilter(raceRankFilter);
            competitorSelectionProvider.setCompetitorsFilterSet(activeFilterSet);
        }
    }

    @Override
    protected void processAutoExpands(AbstractSortableColumnWithMinMax<?, ?> c, RaceColumn<?> lastRaceColumn) {
    }

    @Override
    protected void updateExpansionStates(boolean expand) {
    }

    /**
     * Extracts the rows to display of the <code>leaderboard</code>. These are all {@link AbstractLeaderboardDTO#rows
     * rows} in case {@link #preSelectedRace} is <code>null</code>, or only the rows of the competitors who scored in
     * the race identified by {@link #preSelectedRace} otherwise.
     */
    @Override
    public Map<CompetitorDTO, LeaderboardRowDTO> getRowsToDisplay() {
        Map<CompetitorDTO, LeaderboardRowDTO> result;
        Iterable<CompetitorDTO> allFilteredCompetitors = competitorSelectionProvider.getFilteredCompetitors();
        result = new HashMap<CompetitorDTO, LeaderboardRowDTO>();
        for (CompetitorDTO competitor : leaderboard.rows.keySet()) {
            if (Util.contains(allFilteredCompetitors, competitor)) {
                result.put(competitor, leaderboard.rows.get(competitor));
            }
        }
        return result;
    }
}
