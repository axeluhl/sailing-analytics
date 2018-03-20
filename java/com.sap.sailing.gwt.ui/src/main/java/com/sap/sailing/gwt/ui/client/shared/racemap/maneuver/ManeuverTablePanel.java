package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.SailingPermissionsForRoleProvider;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ManeuverTypeFormatter;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sailing.gwt.ui.leaderboard.HasStringAndDoubleValue;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel.LeaderBoardStyle;
import com.sap.sailing.gwt.ui.leaderboard.MinMaxRenderer;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTableWithStylableHeaders;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomModel;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

public class ManeuverTablePanel extends AbstractCompositeComponent<ManeuverTableSettings>
        implements CompetitorSelectionChangeListener, TimeListener {

    private final ManeuverTablePanelResources resources = GWT.create(ManeuverTablePanelResources.class);

    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final StringMessages stringMessages;
    private final CompetitorSelectionProvider competitorSelectionModel;

    private final NumberFormat towDigitAccuracy = NumberFormatterFactory.getDecimalFormat(2);
    private final DateTimeFormat dateformat = DateTimeFormat.getFormat("HH:mm:ss");

    private final SimplePanel contentPanel = new SimplePanel();
    private final Label importantMessageLabel = new Label();
    private final SortedCellTableWithStylableHeaders<ManeuverTableData> maneuverCellTable;
    private final SortableColumn<ManeuverTableData, String> competitorColumn, timeColumn;
    private final Timer timer;
    private final TimeRangeWithZoomModel timeRangeWithZoomProvider;
    private final CompetitorManeuverDataCache competitorManeuverDataCache = new CompetitorManeuverDataCache();
    
    private ManeuverTableSettings settings;
    private boolean hasCanReplayDuringLiveRacesPermission;

    public ManeuverTablePanel(Component<?> parent, ComponentContext<?> context,
            final SailingServiceAsync sailingService, final RegattaAndRaceIdentifier raceIdentifier,
            final StringMessages stringMessages, final CompetitorSelectionProvider competitorSelectionModel,
            final ErrorReporter errorReporter, final Timer timer, ManeuverTableSettings initialSettings,
            TimeRangeWithZoomModel timeRangeWithZoomProvider, LeaderBoardStyle style, UserService userService) {
        super(parent, context);
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                hasCanReplayDuringLiveRacesPermission = user != null
                        && user.hasPermission(Permission.CAN_REPLAY_DURING_LIVE_RACES.getStringPermission(),
                                SailingPermissionsForRoleProvider.INSTANCE);
            }
        });
        this.resources.css().ensureInjected();
        this.settings = initialSettings;
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.competitorSelectionModel = competitorSelectionModel;
        this.stringMessages = stringMessages;
        this.timer = timer;
        this.timeRangeWithZoomProvider = timeRangeWithZoomProvider;
        this.competitorSelectionModel.addCompetitorSelectionChangeListener(this);
        this.timer.addTimeListener(this);
        final FlowPanel rootPanel = new FlowPanel();
        rootPanel.addStyleName(resources.css().maneuverPanel());
        this.contentPanel.addStyleName(resources.css().contentContainer());
        rootPanel.add(contentPanel);
        final Button settingsButton = SettingsDialog.createSettingsButton(this, stringMessages);
        settingsButton.setStyleName(resources.css().settingsButton());
        rootPanel.add(settingsButton);
        this.importantMessageLabel.addStyleName(resources.css().importantMessage());
        this.maneuverCellTable = new SortedCellTableWithStylableHeaders<>(Integer.MAX_VALUE, style.getTableresources());
        this.maneuverCellTable.addStyleName(resources.css().maneuverTable());
        final SingleSelectionModel<ManeuverTableData> selectionModel = new SingleSelectionModel<>();
        this.maneuverCellTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final ManeuverTableData selected = selectionModel.getSelectedObject();
                if (selected != null && (isReplaying() || hasCanReplayDuringLiveRacesPermission)) {
                    timer.pause();
                    timer.setTime(selected.getTimePointBefore().getTime());
                } else if (selected != null) {
                    selectionModel.clear();
                }
            }
        });
        this.maneuverCellTable.addColumn(competitorColumn = createCompetitorColumn());
        this.maneuverCellTable.addColumn(createManeuverTypeColumn());
        this.maneuverCellTable.addColumn(timeColumn = createTimeColumn());
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getDurationAsSeconds,
                this.stringMessages.durationPlain(), this.stringMessages.secondsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getSpeedInAsKnots,
                this.stringMessages.speedIn(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getSpeedOutAsKnots,
                this.stringMessages.speedOut(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getMinSpeedAsKnots,
                this.stringMessages.minSpeed(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable
                .addColumn(createSortableMinMaxColumn(ManeuverTableData::getTurnRate, this.stringMessages.turnRate(),
                        this.stringMessages.degreesUnit() + "/" + this.stringMessages.secondsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getLoss,
                this.stringMessages.maneuverLoss(), stringMessages.metersUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getDirectionChange,
                stringMessages.maneuverAngle(), this.stringMessages.degreesUnit()));
        initWidget(rootPanel);
        setVisible(false);
    }

    private SortableColumn<ManeuverTableData, String> createSortableMinMaxColumn(
            Function<ManeuverTableData, Double> extractor, String title, String unit) {
        final SortableColumn<ManeuverTableData, String> col = new AbstractSortableColumnWithMinMax<ManeuverTableData, String>(
                new TextCell(), SortingOrder.ASCENDING) {
            final InvertibleComparator<ManeuverTableData> comparatorWithAbs = new InvertibleComparatorAdapter<ManeuverTableData>() {
                @Override
                public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                    Double o1v = extractor.apply(o1);
                    Double o2v = extractor.apply(o2);
                    if (o1v == null && o2v == null) {
                        return 0;
                    }
                    if (o1v == null && o2v != null) {
                        return -1;
                    }
                    if (o1v != null && o2v == null) {
                        return 1;
                    }
                    return Double.compare(Math.abs(o1v), Math.abs(o2v));
                }
            };

            final HasStringAndDoubleValue<ManeuverTableData> dataProvider = new HasStringAndDoubleValue<ManeuverTableData>() {
                @Override
                public String getStringValueToRender(ManeuverTableData row) {
                    Double value = extractor.apply(row);
                    if (value == null) {
                        return null;
                    }
                    return towDigitAccuracy.format(value);
                }

                @Override
                public Double getDoubleValue(ManeuverTableData row) {
                    Double value = extractor.apply(row);
                    return value == null ? null : Math.abs(value);
                }
            };

            final MinMaxRenderer<ManeuverTableData> renderer = new MinMaxRenderer<ManeuverTableData>(dataProvider,
                    comparatorWithAbs);

            @Override
            public InvertibleComparator<ManeuverTableData> getComparator() {
                return comparatorWithAbs;
            }

            @Override
            public void render(Context context, ManeuverTableData object, SafeHtmlBuilder sb) {
                renderer.render(context, object, title, sb);
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(title + " [" + unit + "]");
            }

            @Override
            public String getValue(ManeuverTableData object) {
                return dataProvider.getStringValueToRender(object);
            }

            @Override
            public void updateMinMax() {
                renderer.updateMinMax(maneuverCellTable.getDataProvider().getList());
            }
        };
        col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        return col;
    }

    private SortableColumn<ManeuverTableData, String> createManeuverTypeColumn() {
        return new SortableColumn<ManeuverTableData, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<ManeuverTableData> getComparator() {
                return new InvertibleComparatorAdapter<ManeuverTableData>() {
                    @Override
                    public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                        return o1.getManeuverType().compareTo(o2.getManeuverType());
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.maneuverType());
            }

            @Override
            public String getValue(ManeuverTableData object) {
                return ManeuverTypeFormatter.format(object.getManeuverType(), stringMessages);
            }
        };
    }

    private SortableColumn<ManeuverTableData, String> createTimeColumn() {

        final InvertibleComparator<ManeuverTableData> comparator = new InvertibleComparatorAdapter<ManeuverTableData>() {
            @Override
            public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                return o1.getTimePoint().compareTo(o2.getTimePoint());
            }
        };

        final SortableColumn<ManeuverTableData, String> col = new SortableColumn<ManeuverTableData, String>(
                new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<ManeuverTableData> getComparator() {
                return comparator;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.time());
            }

            @Override
            public String getValue(ManeuverTableData object) {
                return dateformat.format(object.getTimePoint());
            }
        };
        col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        return col;
    }

    private SortableColumn<ManeuverTableData, String> createCompetitorColumn() {
        InvertibleComparator<ManeuverTableData> comparator = new InvertibleComparatorAdapter<ManeuverTableData>() {
            @Override
            public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                return o1.getCompetitor().getName().compareTo(o2.getCompetitor().getName());
            }
        };

        return new SortableColumn<ManeuverTableData, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<ManeuverTableData> getComparator() {
                return comparator;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.competitor());
            }

            @Override
            public String getValue(ManeuverTableData object) {
                return object.getCompetitor().getName();
            }
        };
    }

    private void rerender() {
        if (isVisible()) {
            if (Util.isEmpty(competitorSelectionModel.getSelectedCompetitors())) {
                this.importantMessageLabel.setText(stringMessages.selectAtLeastOneCompetitorManeuver());
                this.contentPanel.setWidget(importantMessageLabel);
            } else if (competitorManeuverDataCache.isEmpty()) {
                this.importantMessageLabel.setText(stringMessages.noDataFound());
                this.contentPanel.setWidget(importantMessageLabel);
            } else {
                this.contentPanel.setWidget(maneuverCellTable);
                this.showCompetitorColumn(Util.size(competitorSelectionModel.getSelectedCompetitors()) != 1);
                this.updateManeuverTableData();
                this.updateManeuverTableColumnsWithMinMax();
                this.maneuverCellTable.restoreColumnSortInfos(timeColumn);
                this.maneuverCellTable.redraw();
            }
        }
    }

    private void showCompetitorColumn(boolean show) {
        if (show && maneuverCellTable.getColumnIndex(competitorColumn) == -1) {
            maneuverCellTable.insertColumn(0, competitorColumn);
        } else if (!show && maneuverCellTable.getColumnIndex(competitorColumn) > -1) {
            maneuverCellTable.removeColumn(competitorColumn);
        }
    }

    private void updateManeuverTableData() {
        final ArrayList<ManeuverTableData> data = new ArrayList<>();
        for (final Entry<CompetitorDTO, CompetitorManeuverData> entry : competitorManeuverDataCache.getCachedData()) {
            for (ManeuverDTO maneuver : entry.getValue().getManeuvers()) {
                if (settings.getSelectedManeuverTypes().contains(maneuver.type)) {
                    data.add(new ManeuverTableData(entry.getKey(), maneuver));
                }
            }
        }
        this.maneuverCellTable.setList(data);
    }

    private void updateManeuverTableColumnsWithMinMax() {
        for (int i = 0; i < maneuverCellTable.getColumnCount(); i++) {
            final Column<ManeuverTableData, ?> column = maneuverCellTable.getColumn(i);
            if (column instanceof AbstractSortableColumnWithMinMax) {
                ((AbstractSortableColumnWithMinMax<ManeuverTableData, ?>) column).updateMinMax();
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        final boolean wasVisible = isVisible();
        super.setVisible(visible);
        if (wasVisible && !visible) {
            this.competitorManeuverDataCache.resetAll();
        } else if (!wasVisible && visible) {
            this.rerender();
            this.competitorManeuverDataCache.updateAll(competitorSelectionModel.getSelectedCompetitors());
        }
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        if (isVisible()) {
            this.competitorManeuverDataCache.update(competitor);
        }
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        if (isVisible()) {
            this.competitorManeuverDataCache.reset(competitor);
            this.rerender();
        }
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.maneuverTable();
    }

    @Override
    public String getDependentCssClassName() {
        return "table";
    }

    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
    }

    @Override
    public ManeuverTableSettings getSettings() {
        return settings;
    }

    @Override
    public String getId() {
        return ManeuverTableLifecycle.ID;
    }

    @Override
    public SettingsDialogComponent<ManeuverTableSettings> getSettingsDialogComponent(
            ManeuverTableSettings useTheseSettings) {
        return new ManeuverTableSettingsDialogComponent(useTheseSettings, stringMessages);
    }

    @Override
    public void updateSettings(ManeuverTableSettings newSettings) {
        settings = newSettings;
        rerender();
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (isVisible()) {
            this.competitorManeuverDataCache.updateAll(competitorSelectionModel.getSelectedCompetitors(), newTime);
        }
    }
    
    private boolean isReplaying() {
        return this.timer.getPlayMode() == PlayModes.Replay;
    }

    private TimeRange getFullTimeRange() {
        final TimePoint from = new MillisecondsTimePoint(timeRangeWithZoomProvider.getFromTime());
        final TimePoint to = new MillisecondsTimePoint(timeRangeWithZoomProvider.getToTime());
        return new TimeRangeImpl(from, to, true);
    }

    private final class CompetitorManeuverDataCache {
        private final Map<CompetitorDTO, CompetitorManeuverData> cache = new HashMap<>();

        private void updateAll(final Iterable<CompetitorDTO> competitors, final Date newTime) {
            this.loadData(competitors, c -> getData(c).requiresUpdate(newTime));
        }

        private void updateAll(final Iterable<CompetitorDTO> competitors) {
            final TimeRange timeRange = getFullTimeRange();
            this.loadData(competitors, c -> Optional.of(timeRange));
        }

        private void update(final CompetitorDTO competitor) {
            final TimeRange timeRange = getFullTimeRange();
            this.loadData(Collections.singleton(competitor), c -> Optional.of(timeRange));
        }

        private void resetAll() {
            this.cache.clear();
        }

        private void reset(final CompetitorDTO competitor) {
            this.cache.remove(competitor);
        }

        private boolean isEmpty() {
            return this.cache.isEmpty();
        }

        private CompetitorManeuverData getData(final CompetitorDTO competitor) {
            return this.cache.computeIfAbsent(competitor, c -> new CompetitorManeuverData());
        }

        private void loadData(final Iterable<CompetitorDTO> competitors,
                final Function<CompetitorDTO, Optional<TimeRange>> timeRangeProvider) {
            if (!Util.isEmpty(competitors)) {
                final Map<CompetitorDTO, TimeRange> competitorToTimeRange = new HashMap<>();
                competitors.forEach(c -> timeRangeProvider.apply(c).ifPresent(tr -> competitorToTimeRange.put(c, tr)));
                sailingService.getManeuvers(raceIdentifier, competitorToTimeRange,
                        new AsyncCallback<Map<CompetitorDTO, List<ManeuverDTO>>>() {

                            @Override
                            public void onSuccess(Map<CompetitorDTO, List<ManeuverDTO>> result) {
                                if (isVisible()) {
                                    for (final Entry<CompetitorDTO, List<ManeuverDTO>> entry : result.entrySet()) {
                                        final CompetitorManeuverData data = getData(entry.getKey());
                                        final TimeRange timeRange = competitorToTimeRange.get(entry.getKey());
                                        data.update(timeRange.from(), timeRange.to(), entry.getValue());
                                    }
                                    ManeuverTablePanel.this.rerender();
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                CompetitorManeuverDataCache.this.resetAll();
                                ManeuverTablePanel.this.rerender();
                            }
                        });
            }
        }

        private Set<Entry<CompetitorDTO, CompetitorManeuverData>> getCachedData() {
            return this.cache.entrySet();
        }

    }

    private class CompetitorManeuverData {
        /**
         * The time point, as millisecond time stamp, of the start of the time range for which
         * maneuvers have been requested; or {@code null} in case ...
         * TODO When can this be null? Upon an uninitialized TimePanel? Upon an open-ended time range? Or only if no request has been made yet?
         */
        private Long earliestRequestMillis;

        /**
         * The time point, as millisecond time stamp, of the end of the time range for which
         * maneuvers have been requested; or {@code null} in case ...
         * TODO When can this be null? Upon an uninitialized TimePanel? Upon an open-ended time range? Or only if no request has been made yet?
         */
        private Long latestRequestMillis;
        
        /**
         * The maneuvers retrieved for one specific competitor for the time range between {@link #earliestRequestMillis}
         * and {@link #latestRequestMillis} so far. Note that the order of maneuvers in this list is not defined; in
         * particular, they will not necessarily be represented in the order of ascending {@link ManeuverDTO#timePoint
         * time points}.
         */
        private final List<ManeuverDTO> maneuvers = new ArrayList<>();

        private void update(final TimePoint earliest, final TimePoint latest, final List<ManeuverDTO> maneuvers) {
            this.earliestRequestMillis = earliest.asMillis();
            this.latestRequestMillis = latest.asMillis();
            this.maneuvers.addAll(maneuvers);
        }

        private Iterable<ManeuverDTO> getManeuvers() {
            return this.maneuvers;
        }

        private Optional<TimeRange> requiresUpdate(final Date newTime) {
            final Optional<TimeRange> result;
            if (isReplaying() && latestRequestMillis == null) {
                result = Optional.of(getFullTimeRange());
            } else if (earliestRequestMillis == null) {
                final TimePoint from = new MillisecondsTimePoint(timeRangeWithZoomProvider.getFromTime());
                final TimePoint to = new MillisecondsTimePoint(newTime);
                result = Optional.of(new TimeRangeImpl(from, to, true));
            } else if (newTime.getTime() < earliestRequestMillis) {
                final TimePoint from = new MillisecondsTimePoint(timeRangeWithZoomProvider.getFromTime());
                final TimePoint to = new MillisecondsTimePoint(earliestRequestMillis);
                result = Optional.of(new TimeRangeImpl(from, to, true));
            } else if (latestRequestMillis != null && newTime.getTime() > latestRequestMillis) {
                final TimePoint from = new MillisecondsTimePoint(latestRequestMillis);
                final TimePoint to = new MillisecondsTimePoint(timeRangeWithZoomProvider.getToTime());
                result = Optional.of(new TimeRangeImpl(from, to, true));
            } else {
                result = Optional.empty();
            }
            return result;
        }
    }

}
