package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.SailingPermissionsForRoleProvider;
import com.sap.sailing.gwt.ui.actions.GetManeuversForCompetitorsAction;
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
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.TimeRangeProvider;
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

    private static final Supplier<Long> LOADING_OFFSET_TO_NEXT_MANEUVER_PROVIDER = () -> 2500L;

    private final ManeuverTablePanelResources resources = GWT.create(ManeuverTablePanelResources.class);

    private final StringMessages stringMessages;
    private final CompetitorSelectionProvider competitorSelectionModel;

    private final NumberFormat towDigitAccuracy = NumberFormatterFactory.getDecimalFormat(2);

    private final SimplePanel contentPanel = new SimplePanel();
    private final Label importantMessageLabel = new Label();
    private final SortedCellTableWithStylableHeaders<ManeuverTableData> maneuverCellTable;
    private final SortableColumn<ManeuverTableData, ?> competitorColumn, timeColumn;
    private final CachedRaceDataProvider<CompetitorWithBoatDTO, ManeuverDTO> competitorDataProvider;

    private ManeuverTableSettings settings;
    private boolean hasCanReplayDuringLiveRacesPermission;

    public ManeuverTablePanel(final Component<?> parent, ComponentContext<?> context,
            final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final RegattaAndRaceIdentifier raceIdentifier, final StringMessages stringMessages,
            final CompetitorSelectionProvider competitorSelectionModel, final ErrorReporter errorReporter,
            final Timer timer, final ManeuverTableSettings initialSettings,
            final TimeRangeWithZoomModel timeRangeWithZoomProvider, final LeaderBoardStyle style,
            final UserService userService) {
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
        this.competitorSelectionModel = competitorSelectionModel;
        this.stringMessages = stringMessages;
        this.competitorDataProvider = new CachedManeuverTableDataProvider(timeRangeWithZoomProvider, timer,
                raceIdentifier, sailingService, asyncActionsExecutor);
        this.competitorSelectionModel.addCompetitorSelectionChangeListener(this);
        timer.addTimeListener(this);
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
                if (selected != null
                        && (timer.getPlayMode() == PlayModes.Replay || hasCanReplayDuringLiveRacesPermission)) {
                    timer.pause();
                    timer.setTime(selected.getTimePointBefore().getTime());
                } else if (selected != null) {
                    selectionModel.clear();
                }
            }
        });
        this.maneuverCellTable.addColumn(competitorColumn = createCompetitorColumn());
        this.maneuverCellTable.addColumn(createManeuverTypeColumn());
        this.maneuverCellTable.addColumn(createMarkPassingColumn());
        this.maneuverCellTable.addColumn(timeColumn = createTimeColumn());
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getSpeedBeforeInKnots,
                this.stringMessages.speedIn(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getSpeedAfterInKnots,
                this.stringMessages.speedOut(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getSpeedChangeInKnots,
                this.stringMessages.speedChange(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getLowestSpeedInKnots,
                this.stringMessages.lowestSpeed(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getMaximumTurningRate,
                this.stringMessages.maxTurningRate(), this.stringMessages.degreesPerSecondUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getAverageTurningRate,
                this.stringMessages.avgTurningRate(), this.stringMessages.degreesPerSecondUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getManeuverLoss,
                this.stringMessages.maneuverLoss(), stringMessages.metersUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(ManeuverTableData::getDirectionChange,
                stringMessages.directionChange(), this.stringMessages.degreesShort()));
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

    private SortableColumn<ManeuverTableData, Date> createTimeColumn() {

        final InvertibleComparator<ManeuverTableData> comparator = new InvertibleComparatorAdapter<ManeuverTableData>() {
            @Override
            public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                return o1.getTimePoint().compareTo(o2.getTimePoint());
            }
        };

        final SortableColumn<ManeuverTableData, Date> col = new SortableColumn<ManeuverTableData, Date>(
                new DateCell(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG)), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<ManeuverTableData> getComparator() {
                return comparator;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.time());
            }

            @Override
            public Date getValue(ManeuverTableData object) {
                return object.getTimePoint();
            }
        };
        col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        return col;
    }

    private SortableColumn<ManeuverTableData, Boolean> createMarkPassingColumn() {
        final InvertibleComparator<ManeuverTableData> comparator = new InvertibleComparatorAdapter<ManeuverTableData>() {
            public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                return -Boolean.compare(o1.isMarkPassing(), o2.isMarkPassing());
            }
        };

        final SortableColumn<ManeuverTableData, Boolean> column = new SortableColumn<ManeuverTableData, Boolean>(
                new AbstractCell<Boolean>() {
                    @Override
                    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                        sb.append(value ? SafeHtmlUtils.fromTrustedString("&#10004;") : SafeHtmlUtils.EMPTY_SAFE_HTML);
                    }
                }, SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<ManeuverTableData> getComparator() {
                return comparator;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.markPassing());
            }

            @Override
            public Boolean getValue(ManeuverTableData object) {
                return object.isMarkPassing();
            }
        };
        column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        return column;
    }

    private SortableColumn<ManeuverTableData, String> createCompetitorColumn() {
        InvertibleComparator<ManeuverTableData> comparator = new InvertibleComparatorAdapter<ManeuverTableData>() {
            @Override
            public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                return o1.getCompetitorName().compareTo(o2.getCompetitorName());
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
                return object.getCompetitorName();
            }
        };
    }

    private void rerender() {
        if (isVisible()) {
            if (Util.isEmpty(competitorSelectionModel.getSelectedCompetitors())) {
                this.importantMessageLabel.setText(stringMessages.selectAtLeastOneCompetitorManeuver());
                this.contentPanel.setWidget(importantMessageLabel);
            } else if (!competitorDataProvider.hasCachedData()) {
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
        for (final Entry<CompetitorWithBoatDTO, List<ManeuverDTO>> entry : competitorDataProvider.getCachedData()
                .entrySet()) {
            for (ManeuverDTO maneuver : entry.getValue()) {
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
            this.competitorDataProvider.removeAllEntries();
        } else if (!wasVisible && visible) {
            this.rerender();
            this.competitorDataProvider.ensureEntries(competitorSelectionModel.getSelectedCompetitors());
        }
    }

    @Override
    public void addedToSelection(CompetitorWithBoatDTO competitor) {
        if (isVisible()) {
            this.competitorDataProvider.ensureEntry(competitor);
            this.rerender();
        }
    }

    @Override
    public void removedFromSelection(CompetitorWithBoatDTO competitor) {
        if (isVisible()) {
            this.competitorDataProvider.removeEntry(competitor);
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
    public void filterChanged(FilterSet<CompetitorWithBoatDTO, ? extends Filter<CompetitorWithBoatDTO>> oldFilterSet,
            FilterSet<CompetitorWithBoatDTO, ? extends Filter<CompetitorWithBoatDTO>> newFilterSet) {
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorWithBoatDTO> competitors) {
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorWithBoatDTO> filteredCompetitors) {
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
            this.competitorDataProvider.updateEntryData();
        }
    }

    private class CachedManeuverTableDataProvider extends CachedRaceDataProvider<CompetitorWithBoatDTO, ManeuverDTO> {
        private final AsyncActionsExecutor asyncActionsExecutor;
        private final RegattaAndRaceIdentifier raceIdentifier;
        private final SailingServiceAsync sailingService;

        private CachedManeuverTableDataProvider(final TimeRangeProvider timeRangeProvider, final Timer timer,
                final RegattaAndRaceIdentifier raceIdentifier, final SailingServiceAsync sailingService,
                final AsyncActionsExecutor asyncActionsExecutor) {
            super(timeRangeProvider, timer, m -> m.timePoint, LOADING_OFFSET_TO_NEXT_MANEUVER_PROVIDER, true);
            this.asyncActionsExecutor = asyncActionsExecutor;
            this.raceIdentifier = raceIdentifier;
            this.sailingService = sailingService;
        }

        @Override
        protected void loadData(final Map<CompetitorWithBoatDTO, TimeRange> competitorTimeRanges,
                final boolean incremental,
                final AsyncCallback<Map<CompetitorWithBoatDTO, List<ManeuverDTO>>> callback) {
            if (incremental) {
                asyncActionsExecutor.execute(
                        new GetManeuversForCompetitorsAction(sailingService, raceIdentifier, competitorTimeRanges),
                        callback);
            } else {
                // AsyncActionExecutor is explicitly not used here, to ensure full updates are always executed.
                // Because full updates are triggered in specific situations only, this shouldn't cause server overload.
                sailingService.getManeuvers(raceIdentifier, competitorTimeRanges, callback);
            }
        }

        @Override
        protected void onEntriesDataChange(final Iterable<CompetitorWithBoatDTO> updatedCompetitors) {
            ManeuverTablePanel.this.rerender();
        }
    }
}