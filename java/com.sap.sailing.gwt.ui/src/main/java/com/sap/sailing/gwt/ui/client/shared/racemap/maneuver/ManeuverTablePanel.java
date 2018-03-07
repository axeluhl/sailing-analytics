package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
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
    private final SortedCellTableWithStylableHeaders<SingleManeuverDTO> maneuverCellTable;
    private final SortableColumn<SingleManeuverDTO, String> competitorColumn, timeColumn;
    private final Timer timer;
    private final TimeRangeWithZoomModel timeRangeWithZoomProvider;
    private final Map<CompetitorDTO, List<ManeuverDTO>> lastResult = new HashMap<>();
    private ManeuverTableSettings settings;
    protected boolean hasCanReplayDuringLiveRacesPermission;

    public ManeuverTablePanel(Component<?> parent, ComponentContext<?> context,
            final SailingServiceAsync sailingService, final RegattaAndRaceIdentifier raceIdentifier,
            final StringMessages stringMessages, final CompetitorSelectionProvider competitorSelectionModel,
            final ErrorReporter errorReporter, final Timer timer, ManeuverTableSettings initialSettings,
            TimeRangeWithZoomModel timeRangeWithZoomProvider, LeaderBoardStyle style, UserService userService) {
        super(parent, context);
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                hasCanReplayDuringLiveRacesPermission = user != null && user.hasPermission(
                        Permission.CAN_REPLAY_DURING_LIVE_RACES.getStringPermission(), SailingPermissionsForRoleProvider.INSTANCE);
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

        final SingleSelectionModel<SingleManeuverDTO> selectionModel = new SingleSelectionModel<>();
        this.maneuverCellTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final SingleManeuverDTO selected = selectionModel.getSelectedObject();
                if (selected != null && (timer.getPlayMode() == PlayModes.Replay || hasCanReplayDuringLiveRacesPermission)) {
                    timer.pause();
                    timer.setTime(selected.getTime().getTime());
                } else if (selected != null) {
                    selectionModel.clear();
                }
            }
        });

        this.maneuverCellTable.addColumn(competitorColumn = createCompetitorColumn());
        this.maneuverCellTable.addColumn(createManeuverTypeColumn());
        this.maneuverCellTable.addColumn(timeColumn = createTimeColumn());
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getDurationAsSeconds,
                this.stringMessages.durationPlain(), this.stringMessages.secondsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getSpeedInAsKnots,
                this.stringMessages.speedIn(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getSpeedOutAsKnots,
                this.stringMessages.speedOut(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getMinSpeedAsKnots,
                this.stringMessages.minSpeed(), this.stringMessages.knotsUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getTurnRate,
                this.stringMessages.turnRate(), this.stringMessages.degreesUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getLoss,
                this.stringMessages.maneuverLoss(), stringMessages.metersUnit()));
        this.maneuverCellTable.addColumn(createSortableMinMaxColumn(SingleManeuverDTO::getDirectionChangeInDegrees,
                "i18n directionchange", this.stringMessages.degreesUnit()));

        initWidget(rootPanel);
        setVisible(false);
    }

    private SortableColumn<SingleManeuverDTO, String> createSortableMinMaxColumn(
            Function<SingleManeuverDTO, Double> extractor, String title, String unit) {
        final SortableColumn<SingleManeuverDTO, String> col = new AbstractSortableColumnWithMinMax<SingleManeuverDTO, String>(
                new TextCell(), SortingOrder.ASCENDING) {

            final InvertibleComparator<SingleManeuverDTO> comparatorWithAbs = new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                @Override
                public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
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

            final HasStringAndDoubleValue<SingleManeuverDTO> dataProvider = new HasStringAndDoubleValue<SingleManeuverDTO>() {

                @Override
                public String getStringValueToRender(SingleManeuverDTO row) {
                    Double value = extractor.apply(row);
                    if (value == null) {
                        return null;
                    }
                    return towDigitAccuracy.format(value);
                }

                @Override
                public Double getDoubleValue(SingleManeuverDTO row) {
                    Double value = extractor.apply(row);
                    return value == null ? null : Math.abs(value);
                }
            };

            final MinMaxRenderer<SingleManeuverDTO> renderer = new MinMaxRenderer<SingleManeuverDTO>(dataProvider,
                    comparatorWithAbs);

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return comparatorWithAbs;
            }

            @Override
            public void render(Context context, SingleManeuverDTO object, SafeHtmlBuilder sb) {
                renderer.render(context, object, title, sb);
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(title + " [" + unit + "]");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
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

    private SortableColumn<SingleManeuverDTO, String> createManeuverTypeColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.getManeuverType().compareTo(o2.getManeuverType());
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.maneuverType());
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return ManeuverTypeFormatter.format(object.getManeuverType(), stringMessages);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createTimeColumn() {

        final InvertibleComparator<SingleManeuverDTO> comparator = new InvertibleComparatorAdapter<SingleManeuverDTO>() {
            @Override
            public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        };

        final SortableColumn<SingleManeuverDTO, String> col = new SortableColumn<SingleManeuverDTO, String>(
                new TextCell(), SortingOrder.DESCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return comparator;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.time());
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return dateformat.format(object.getTime());
            }
        };
        col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        return col;
    }

    private SortableColumn<SingleManeuverDTO, String> createCompetitorColumn() {
        InvertibleComparator<SingleManeuverDTO> comparator = new InvertibleComparatorAdapter<SingleManeuverDTO>() {
            @Override
            public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                return o1.getCompetitor().getName().compareTo(o2.getCompetitor().getName());
            }
        };

        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return comparator;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(stringMessages.competitor());
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return object.getCompetitor().getName();
            }
        };
    }

    /**
     * Recreate the view, this is meant to save us a remote call if only competitor is changed
     */
    private void rerender() {
        if (isVisible()) {
            if (Util.isEmpty(competitorSelectionModel.getSelectedCompetitors())) {
                this.importantMessageLabel.setText(stringMessages.selectCompetitor());
                this.contentPanel.setWidget(importantMessageLabel);
            } else if (lastResult.isEmpty()) {
                this.importantMessageLabel.setText(stringMessages.noDataFound());
                this.contentPanel.setWidget(importantMessageLabel);
            } else {
                this.contentPanel.setWidget(maneuverCellTable);
                this.showCompetitorColumn(Util.size(competitorSelectionModel.getSelectedCompetitors()) != 1);
                this.maneuverCellTable.setList(convertToTableFormat());
                for (int i = 0; i < maneuverCellTable.getColumnCount(); i++) {
                    final Column<SingleManeuverDTO, ?> column = maneuverCellTable.getColumn(i);
                    if (column instanceof AbstractSortableColumnWithMinMax) {
                        ((AbstractSortableColumnWithMinMax<SingleManeuverDTO, ?>) column).updateMinMax();
                    }
                }
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

    private ArrayList<SingleManeuverDTO> convertToTableFormat() {
        final ArrayList<SingleManeuverDTO> data = new ArrayList<>();
        for (Entry<CompetitorDTO, List<ManeuverDTO>> res : lastResult.entrySet()) {
            if (competitorSelectionModel.isSelected(res.getKey())) {
                for (ManeuverDTO maneuver : res.getValue()) {
                    if (settings.getSelectedManeuverTypes().contains(maneuver.type)) {
                        double turnRate = 0;
                        if (maneuver.duration != null) {
                            turnRate = Math.abs(maneuver.directionChangeInDegrees) / maneuver.duration.asSeconds();
                        }

                        data.add(new SingleManeuverDTO(res.getKey(), maneuver.timepoint, maneuver.type,
                                maneuver.duration, maneuver.speedWithBearingBefore, maneuver.speedWithBearingAfter,
                                maneuver.minSpeed, turnRate, maneuver.maneuverLossInMeters,
                                maneuver.directionChangeInDegrees));
                    }
                }
            }
        }
        return data;
    }

    private void refresh() {
        final Map<CompetitorDTO, Date> from = new HashMap<>();
        final Map<CompetitorDTO, Date> to = new HashMap<>();
        for (CompetitorDTO comp : competitorSelectionModel.getAllCompetitors()) {
            from.put(comp, timeRangeWithZoomProvider.getFromTime());
            to.put(comp, timeRangeWithZoomProvider.getToTime());
        }

        sailingService.getManeuvers(raceIdentifier, from, to,
                new AsyncCallback<Map<CompetitorDTO, List<ManeuverDTO>>>() {

                    @Override
                    public void onSuccess(Map<CompetitorDTO, List<ManeuverDTO>> result) {
                        lastResult.clear();
                        lastResult.putAll(result);
                        rerender();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        lastResult.clear();
                        rerender();
                    }
                });
    }

    @Override
    public void setVisible(boolean visible) {
        if (!isVisible() && visible && lastResult.isEmpty()) {
            this.refresh();
        } else {
            this.rerender();
        }
        super.setVisible(visible);
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        this.rerender();
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        this.rerender();
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
        if (isVisible() && timer.getPlayMode() == PlayModes.Live) {
            refresh();
        }
    }

}
