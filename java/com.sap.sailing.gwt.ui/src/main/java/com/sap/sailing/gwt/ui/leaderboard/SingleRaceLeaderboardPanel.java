package com.sap.sailing.gwt.ui.leaderboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.SingleRaceLeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.SingleRaceLeaderboardSettingsDialogComponent;
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
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class SingleRaceLeaderboardPanel extends LeaderboardPanel<SingleRaceLeaderboardSettings> {
    private boolean autoExpandPreSelectedRace;

    /**
     * If this is <code>null</code>, all leaderboard columns added by updating the leaderboard from the server are
     * automatically added to the table. Otherwise, only the column whose {@link RaceColumnDTO#getRaceIdentifier(String)
     * race identifier} matches the value of this attribute will be added.
     */
    private final RegattaAndRaceIdentifier preSelectedRace;

    private boolean notSortedYet = true;

    public SingleRaceLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, LeaderboardSettings settings,
            boolean isEmbedded, RegattaAndRaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages, boolean showRaceDetails,
            CompetitorFilterPanel competitorSearchTextBox, boolean showSelectionCheckbox,
            RaceTimesInfoProvider optionalRaceTimesInfoProvider, boolean autoExpandLastRaceColumn,
            boolean adjustTimerDelay, boolean autoApplyTopNFilter, boolean showCompetitorFilterStatus,
            boolean enableSyncScroller) {
        super(parent, context, sailingService, asyncActionsExecutor, settings, isEmbedded, competitorSelectionProvider,
                timer, leaderboardGroupName, leaderboardName, errorReporter, stringMessages, showRaceDetails,
                competitorSearchTextBox, showSelectionCheckbox, optionalRaceTimesInfoProvider, autoExpandLastRaceColumn,
                adjustTimerDelay, autoApplyTopNFilter, showCompetitorFilterStatus, enableSyncScroller);
        assert preSelectedRace != null;
        this.preSelectedRace = preSelectedRace;

        initialize(settings);
    }

    @Override
    protected void setDefaultRaceColumnSelection(LeaderboardSettings settings) {
        raceColumnSelection = new ExplicitRaceColumnSelectionWithPreselectedRace(preSelectedRace);
    }

    @Override
    public SingleRaceLeaderboardSettings getSettings() {
        SingleRaceLeaderboardSettings leaderboardSettings = new SingleRaceLeaderboardSettings(selectedManeuverDetails,
                selectedLegDetails, selectedRaceDetails, selectedOverallDetailColumns, timer.getRefreshInterval(),
                raceColumnSelection.getType(), isShowAddedScores(),
                isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), isShowCompetitorSailId(),
                isShowCompetitorFullName(), isShowCompetitorNationality);
        SettingsDefaultValuesUtils.keepDefaults(currentSettings, leaderboardSettings);
        return leaderboardSettings;
    }

    private boolean isAutoExpandPreSelectedRace() {
        return autoExpandPreSelectedRace;
    }

    @Override
    public String getCompetitorColor(CompetitorDTO competitor) {
        return competitorSelectionProvider.getColor(competitor, preSelectedRace).getAsHtml();
    }

    @Override
    public boolean renderBoatColorIfNecessary(CompetitorDTO competitor, SafeHtmlBuilder sb) {
        boolean showBoatColor = !isShowCompetitorFullName() && isEmbedded;
        if (showBoatColor) {
            String competitorColor = competitorSelectionProvider.getColor(competitor, preSelectedRace).getAsHtml();
            sb.appendHtmlConstant("<div style=\"border-bottom: 2px solid " + competitorColor + ";\">");
        }
        return showBoatColor;
    }

    @Override
    public int getLegCount(LeaderboardDTO leaderboardDTO, String raceColumnName) {
        return leaderboardDTO.getLegCount(raceColumnName, preSelectedRace);
    }

    @Override
    protected LeaderboardSettings overrideDefaultsForNamesOfRaceColumns(LeaderboardSettings currentSettings,
            LeaderboardDTO result) {
        return currentSettings;
    }

    @Override
    protected void applyTop30FilterIfCompetitorSizeGreaterEqual40(LeaderboardDTO leaderboard) {
        int maxRaceRank = 30;
        if (leaderboard.competitors.size() >= 40) {
            CompetitorRaceRankFilter raceRankFilter = new CompetitorRaceRankFilter();
            raceRankFilter.setLeaderboardFetcher(this);
            raceRankFilter.setSelectedRace(preSelectedRace);
            raceRankFilter.setQuickRankProvider(this.competitorFilterPanel.getQuickRankProvider());
            raceRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
            raceRankFilter.setValue(maxRaceRank);
            FilterSet<CompetitorDTO, Filter<CompetitorDTO>> activeFilterSet = competitorSelectionProvider
                    .getOrCreateCompetitorsFilterSet(stringMessages.topNCompetitorsByRaceRank(maxRaceRank));
            activeFilterSet.addFilter(raceRankFilter);
            competitorSelectionProvider.setCompetitorsFilterSet(activeFilterSet);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void processAutoExpands(AbstractSortableColumnWithMinMax<?, ?> c, RaceColumn<?> lastRaceColumn) {
        // Toggle pre-selected race, if the setting is set and it isn't open yet, or the last race column if
        // that was requested
        if ((!autoExpandPerformedOnce && isAutoExpandPreSelectedRace() && c instanceof LeaderboardPanel.RaceColumn
                && ((RaceColumn) c).getRace().hasTrackedRace(preSelectedRace))
                || (isAutoExpandLastRaceColumn() && c == lastRaceColumn)) {
            ExpandableSortableColumn<?> expandableSortableColumn = (ExpandableSortableColumn<?>) c;
            if (!expandableSortableColumn.isExpanded()) {
                expandableSortableColumn.changeExpansionState(/* expand */ true);
                autoExpandPerformedOnce = true;
            }
        }
        if (c instanceof RaceColumn && ((RaceColumn) c).getRace().hasTrackedRace(preSelectedRace)) {
            RaceColumnDTO raceColumn = ((RaceColumn) c).getRace();
            informLeaderboardUpdateListenersAboutRaceSelected(preSelectedRace, raceColumn);
        }
    }

    @Override
    protected void postApplySettings(LeaderboardSettings newSettings,
            List<ExpandableSortableColumn<?>> columnsToExpandAgain) {
        super.postApplySettings(newSettings, columnsToExpandAgain);

        if (notSortedYet) {
            final RaceColumn<?> raceColumnByRaceName = getRaceColumnByRaceName(preSelectedRace.getRaceName());
            if (raceColumnByRaceName != null) {
                getLeaderboardTable().sortColumn(raceColumnByRaceName, /* ascending */true);
                notSortedYet = false;
            }
        }
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
        for (CompetitorDTO competitorInPreSelectedRace : getCompetitors(preSelectedRace)) {
            if (Util.contains(allFilteredCompetitors, competitorInPreSelectedRace)) {
                result.put(competitorInPreSelectedRace, leaderboard.rows.get(competitorInPreSelectedRace));
            }
        }
        return result;
    }

    @Override
    public SettingsDialogComponent<SingleRaceLeaderboardSettings> getSettingsDialogComponent(
            SingleRaceLeaderboardSettings useTheseSettings) {
        return new SingleRaceLeaderboardSettingsDialogComponent(useTheseSettings, leaderboard.getNamesOfRaceColumns(),
                stringMessages);
    }

    @Override
    protected void openSettingsDialog() {
        SettingsDialog<SingleRaceLeaderboardSettings> settingsDialog = new SettingsDialog<SingleRaceLeaderboardSettings>(
                this, stringMessages);
        settingsDialog.ensureDebugId("LeaderboardSettingsDialog");
        settingsDialog.show();
    }

    @Override
    protected void applyRaceSelection(LeaderboardSettings newSettings) {
    }

    public void setAutoExpandPreSelected(boolean b) {
        autoExpandPreSelectedRace = b;
    }
}
