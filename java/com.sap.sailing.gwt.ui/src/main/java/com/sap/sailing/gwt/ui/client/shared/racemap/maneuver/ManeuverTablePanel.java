package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.DurationFormatter;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
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
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomModel;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

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
    private final Label selectCompetitorLabel;
    private final SortedCellTableWithStylableHeaders<SingleManeuverDTO> maneuverCellTable;
    private Timer timer;
    private TimeRangeWithZoomModel timeRangeWithZoomProvider;
    private Long timeOfEarliestRequestInMillis;
    private Long timeOfLatestRequestInMillis;
    protected Map<CompetitorDTO, List<ManeuverDTO>> lastResult;
    private Date fromTime;
    private Date newTime;
    private SortableColumn<SingleManeuverDTO, String> turnRateColumn;
    private ManeuverTableSettings settings;

    public ManeuverTablePanel(Component<?> parent, ComponentContext<?> context,
            final SailingServiceAsync sailingService, final RegattaAndRaceIdentifier raceIdentifier,
            final StringMessages stringMessages, final CompetitorSelectionProvider competitorSelectionModel,
            final ErrorReporter errorReporter, final Timer timer, ManeuverTableSettings initialSettings,
            TimeRangeWithZoomModel timeRangeWithZoomProvider, LeaderBoardStyle style) {
        super(parent, context);
        this.resources.css().ensureInjected();
        this.settings = initialSettings;
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.competitorSelectionModel = competitorSelectionModel;
        this.stringMessages = stringMessages;
        this.timer = timer;
        this.timeRangeWithZoomProvider = timeRangeWithZoomProvider;

        competitorSelectionModel.addCompetitorSelectionChangeListener(this);

        final FlowPanel rootPanel = new FlowPanel();
        rootPanel.addStyleName(resources.css().maneuverPanel());
        this.contentPanel.addStyleName(resources.css().contentContainer());
        rootPanel.add(contentPanel);
        final Button settingsButton = SettingsDialog.createSettingsButton(this, stringMessages);
        settingsButton.setStyleName(resources.css().settingsButton());
        rootPanel.add(settingsButton);

        this.selectCompetitorLabel = new Label(stringMessages.selectCompetitor());
        this.selectCompetitorLabel.addStyleName(resources.css().importantMessage());

        maneuverCellTable = new SortedCellTableWithStylableHeaders<>(Integer.MAX_VALUE, style.getTableresources());
        maneuverCellTable.addStyleName(resources.css().maneuverTable());

        SortableColumn<SingleManeuverDTO, String> competitorColumn = createCompetitorColumn();
        maneuverCellTable.addColumn(competitorColumn);

        SortableColumn<SingleManeuverDTO, String> maneuvertypeColumn = createManeuverTypeColumn();
        maneuverCellTable.addColumn(maneuvertypeColumn);

        SortableColumn<SingleManeuverDTO, String> timeColumn = createTimeColumn();
        maneuverCellTable.addColumn(timeColumn);

        SortableColumn<SingleManeuverDTO, String> durationColumn = createDurationColumn();
        maneuverCellTable.addColumn(durationColumn);

        SortableColumn<SingleManeuverDTO, String> speedInColumn = createSpeedInColumn();
        maneuverCellTable.addColumn(speedInColumn);

        SortableColumn<SingleManeuverDTO, String> speedOutColumn = createSpeedOutColumn();
        maneuverCellTable.addColumn(speedOutColumn);

        SortableColumn<SingleManeuverDTO, String> minSpeedColumn = createMinSpeedColumn();
        maneuverCellTable.addColumn(minSpeedColumn);

        turnRateColumn = createTurnRateColumn();
        maneuverCellTable.addColumn(turnRateColumn);

        SortableColumn<SingleManeuverDTO, String> lossColumn = createLossColumn();
        maneuverCellTable.addColumn(lossColumn);

        SortableColumn<SingleManeuverDTO, String> directionColumn = createDirectionColumn();
        maneuverCellTable.addColumn(directionColumn);

        // maneuverCellTable.setVisible(false);
        rootPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
        initWidget(rootPanel);
        setVisible(false);
        clearCacheAndReload();
    }

    private void clearCacheAndReload() {
        timeOfEarliestRequestInMillis = null;
        timeOfLatestRequestInMillis = null;
        refresh(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime());
    }

    private SortableColumn<SingleManeuverDTO, String> createDirectionColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.direction.compareTo(o2.direction);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n direction");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return String.valueOf(object.direction);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createLossColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return Double.compare(o1.loss, o2.loss);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n loss");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return String.valueOf(object.loss);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createTurnRateColumn() {
        return new AbstractSortableColumnWithMinMax<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            InvertibleComparatorAdapter<SingleManeuverDTO> comparator = new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                @Override
                public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                    return Double.compare(o1.turnRate, o2.turnRate);
                }
            };
            
            HasStringAndDoubleValue<SingleManeuverDTO> dataProvider = new HasStringAndDoubleValue<SingleManeuverDTO>() {
                
                @Override
                public String getStringValueToRender(SingleManeuverDTO row) {
                    return towDigitAccuracy.format(row.turnRate);
                }

                @Override
                public Double getDoubleValue(SingleManeuverDTO row) {
                    return row.turnRate;
                }
            };
            
            MinMaxRenderer<SingleManeuverDTO> renderer = new MinMaxRenderer<SingleManeuverDTO>(dataProvider, comparator);
            
            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return comparator;
            }
            
            @Override
            public void render(Context context, SingleManeuverDTO object, SafeHtmlBuilder sb) {
                renderer.render(context, object, "i18n turnratet", sb);
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n turnRate");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return String.valueOf(object.turnRate);
            }

            @Override
            public void updateMinMax() {
                renderer.updateMinMax(maneuverCellTable.getVisibleItems());
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createMinSpeedColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return Double.compare(o1.minSpeed.speedInKnots, o2.minSpeed.speedInKnots);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n minSpeed");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return String.valueOf(object.minSpeed.speedInKnots);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createSpeedOutColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return Double.compare(o1.speedOut.speedInKnots, o2.speedOut.speedInKnots);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n speedOut");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return String.valueOf(stringMessages.knotsValue(object.speedOut.speedInKnots));
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createSpeedInColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return Double.compare(o1.speedIn.speedInKnots, o2.speedIn.speedInKnots);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n speedIn");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return String.valueOf(stringMessages.knotsValue(object.speedIn.speedInKnots));
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createDurationColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            DurationFormatter duration = DurationFormatter.getInstance(true, true);

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.duration.compareTo(o2.duration);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n duration");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return duration.format(object.duration);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createManeuverTypeColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.maneuverType.compareTo(o2.maneuverType);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n Type");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return ManeuverTypeFormatter.format(object.maneuverType, stringMessages);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createTimeColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.time.compareTo(o2.time);
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n Time");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return dateformat.format(object.time);
            }
        };
    }

    private SortableColumn<SingleManeuverDTO, String> createCompetitorColumn() {
        return new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>() {
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.competitor.getName().compareTo(o2.competitor.getName());
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n Competitor");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return object.competitor.getName();
            }
        };
    }

    /**
     * Recreate the view, this is meant to save us a remote call if only competitor is changed
     */
    private void rerender(){
        if (lastResult == null || fromTime == null || newTime == null) {
            refresh(fromTime, newTime);
        } else {
            ArrayList<SingleManeuverDTO> data = convertToTableFormat();
            maneuverCellTable.setList(data);
            for (int i = 0; i < maneuverCellTable.getColumnCount(); i++) {
                Column<SingleManeuverDTO, ?> column = maneuverCellTable.getColumn(i);
                if(column instanceof AbstractSortableColumnWithMinMax){
                    AbstractSortableColumnWithMinMax<SingleManeuverDTO, ?> c = (AbstractSortableColumnWithMinMax<SingleManeuverDTO, ?>) column;
                    c.updateMinMax();
                }
            }
        }
    }

    private ArrayList<SingleManeuverDTO> convertToTableFormat() {
        ArrayList<SingleManeuverDTO> data = new ArrayList<>();
        for (Entry<CompetitorDTO, List<ManeuverDTO>> res : lastResult.entrySet()) {
            if (competitorSelectionModel.isSelected(res.getKey())) {
                for (ManeuverDTO maneuver : res.getValue()) {
                    if (settings.getSelectedManeuverTypes().contains(maneuver.type)) {
                        data.add(new SingleManeuverDTO(res.getKey(), maneuver.timepoint, maneuver.type,
                                Duration.NULL, maneuver.speedWithBearingBefore, maneuver.speedWithBearingAfter,
                                new SpeedWithBearingDTO(0, 0), maneuver.directionChangeInDegrees, 123,
                                Bearing.NORTH));
                    }
                }
            }
        }
        return data;
    }
    
    /**
     * Load new remote data 
     */
    private void refresh(Date fromTime, Date newTime) {
        if(!isVisible()){
            return;
        }
        this.fromTime = fromTime;
        this.newTime = newTime;
        Map<CompetitorDTO, Date> from = new HashMap<>();
        Map<CompetitorDTO, Date> to = new HashMap<>();
        for (CompetitorDTO comp : competitorSelectionModel.getAllCompetitors()) {
            from.put(comp, fromTime);
            to.put(comp, newTime);
        }

        sailingService.getManeuvers(raceIdentifier, from, to,
                new AsyncCallback<Map<CompetitorDTO, List<ManeuverDTO>>>() {

                    @Override
                    public void onSuccess(Map<CompetitorDTO, List<ManeuverDTO>> result) {
                        lastResult = result;
                        rerender();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert("Could not do stuff");
                    }
                });
    }

    @Override
    public void setVisible(boolean visible) {
        processCompetitorSelectionChange(visible);
        super.setVisible(visible);
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        processCompetitorSelectionChange(isVisible());
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        processCompetitorSelectionChange(isVisible());
    }

    private void processCompetitorSelectionChange(boolean visible) {
        if (visible && !Util.isEmpty(competitorSelectionModel.getSelectedCompetitors())) {
            this.contentPanel.setWidget(maneuverCellTable);
            this.rerender();
        } else {
            this.contentPanel.setWidget(selectCompetitorLabel);
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
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return null;
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
        return new ManeuverTableSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(ManeuverTableSettings newSettings) {
        settings = newSettings;
        rerender();
    }

    /**
     * If in live mode, fetches what's missing since the last fix and <code>date</code>. If nothing has been loaded yet,
     * loads from the beginning up to <code>date</code>. If in replay mode, checks if anything has been loaded at all.
     * If not, everything for the currently selected race is loaded; otherwise, no-op.
     */
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (isVisible()) {
            switch (timer.getPlayMode()) {
            case Live: {
                // is date before first cache entry or is cache empty?
                if (timeOfEarliestRequestInMillis == null || newTime.getTime() < timeOfEarliestRequestInMillis) {
                    refresh(timeRangeWithZoomProvider.getFromTime(), newTime);
                } else if (newTime.getTime() > timeOfLatestRequestInMillis) {
                    refresh(new Date(timeOfLatestRequestInMillis), timeRangeWithZoomProvider.getToTime());
                }
                // otherwise the cache spans across date and so we don't need to load anything
                break;
            }
            case Replay: {
                if (timeOfLatestRequestInMillis == null) {
                    // pure replay mode
                    refresh(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime());
                } else {
                    // replay mode during live play
                    if (timeOfEarliestRequestInMillis == null || newTime.getTime() < timeOfEarliestRequestInMillis) {
                        refresh(timeRangeWithZoomProvider.getFromTime(), newTime);
                    } else if (newTime.getTime() > timeOfLatestRequestInMillis) {
                        refresh(new Date(timeOfLatestRequestInMillis), newTime);
                    }
                }
                break;
            }
            }
        }
    }

}
