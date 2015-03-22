package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.actions.GetLeaderboardByNameAction;
import com.sap.sailing.gwt.ui.client.Collator;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.IsEmbeddableComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorRaceRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.leaderboard.DetailTypeColumn.LegDetailField;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.player.PlayStateListener;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A leaderboard essentially consists of a table widget that in its columns displays the entries.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardPanel extends SimplePanel implements TimeListener, PlayStateListener, DisplayedLeaderboardRowsProvider,
        Component<LeaderboardSettings>, IsEmbeddableComponent, CompetitorSelectionChangeListener, LeaderboardFetcher {
    public static final String LOAD_LEADERBOARD_DATA_CATEGORY = "loadLeaderboardData";

    protected static final NumberFormat scoreFormat = NumberFormat.getFormat("0.##");

    private final SailingServiceAsync sailingService;

    private static String IS_LIVE_TEXT_COLOR = "#1876B3";
    private static String DEFAULT_TEXT_COLOR = "#000000";
    
    private static final String STYLE_LEADERBOARD_CONTENT = "leaderboardContent";
    private static final String STYLE_LEADERBOARD_INFO = "leaderboardInfo";
    private static final String STYLE_LEADERBOARD_TOOLBAR = "leaderboardContent-toolbar";
    private static final String STYLE_LEADERBOARD_LIVE_RACE = "leaderboardContent-liverace";
	
    interface RaceColumnTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div style=\"color:{0}; border-bottom: 3px solid {1}\">")
        SafeHtml cellFrameWithTextColorAndFleetBorder(String textColor, String borderStyle);

        @SafeHtmlTemplates.Template("<div style=\"color:{0};\">")
        SafeHtml cellFrameWithTextColor(String textColor);
    }

    private static RaceColumnTemplates raceColumnTemplate = GWT.create(RaceColumnTemplates.class);

    /**
     * The leaderboard name is used to
     * {@link SailingServiceAsync#getLeaderboardByName(String, java.util.Date, String[], boolean, String, com.google.gwt.user.client.rpc.AsyncCallback)
     * obtain the leaderboard contents} from the server. It may change in case the leaderboard is renamed.
     */
    private String leaderboardName;

    private final ErrorReporter errorReporter;

    private final StringMessages stringMessages;

    private final SortedCellTable<LeaderboardRowDTO> leaderboardTable;

    private final MultiSelectionModel<LeaderboardRowDTO> leaderboardSelectionModel;

    private LeaderboardDTO leaderboard;

    private final TotalRankColumn totalRankColumn;
    
    private final SelectionCheckboxColumn<LeaderboardRowDTO> selectionCheckboxColumn;

    /**
     * Passed to the {@link ManeuverCountRaceColumn}. Modifications to this list will modify the column's children list
     * when updated the next time.
     */
    private final List<DetailType> selectedManeuverDetails;

    /**
     * Passed to the {@link LegColumn}. Modifications to this list will modify the column's children list when updated
     * the next time.
     */
    private final List<DetailType> selectedLegDetails;

    /**
     * Passed to the {@link TextRaceColumn}. Modifications to this list will modify the column's children list when
     * updated the next time.
     */
    private final List<DetailType> selectedRaceDetails;

    private final List<DetailType> selectedOverallDetailColumns;
    
    private final Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> overallDetailColumnMap;

    private RaceColumnSelection raceColumnSelection;

    protected final String RACE_COLUMN_HEADER_STYLE;

    protected final String LEG_DETAIL_COLUMN_HEADER_STYLE;

    protected final String LEG_DETAIL_COLUMN_STYLE;

    protected final String LEG_COLUMN_HEADER_STYLE;

    protected final String RACE_COLUMN_STYLE;

    protected final String LEG_COLUMN_STYLE;

    protected final String TOTAL_COLUMN_STYLE;

    private final Timer timer;
    
    /**
     * A {@link LeaderboardDTO} tells something about the live delay through its {@link LeaderboardDTO#getDelayToLiveInMillisForLatestRace()} method.
     * If this flag is <code>true</code>, the live delay in the {@link #timer} will be adjusted each time the {@link #updateLeaderboard(LeaderboardDTO)}
     * method is invoked. Otherwise, the timer's delay will be left alone which is helpful, e.g., if the leaderboard panel is embedded
     * in a race board panel that focuses on one particular race which may not be the same as the one controlling the leaderboard's overall
     * live delay.
     */
    private final boolean adjustTimerDelay;

    private boolean autoExpandPreSelectedRace;
    
    private boolean autoExpandLastRaceColumn;
    
    /**
     * When <code>true</code>, the race columns don't display the competitors' scores in the race represented by the column
     * but the cumulative score up to that race.
     */
    private boolean showAddedScores;
    
    private boolean showCompetitorSailId;
    private boolean showCompetitorFullName;

    /**
     * When <code>true</code> then an additional column just before the overall points is displayed that sums up
     * the number of races sailed per competitor.
     */
    private boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor;

    /**
     * Remembers whether the auto-expand of the pre-selected race (see {@link #autoExpandPreSelectedRace}) or last
     * selected race {@link #autoExpandLastRaceColumn} has been performed once. It must not be performed another time.
     */
    private boolean autoExpandPerformedOnce;

    /**
     * This anchor's HTML holds the image tag for the play/pause button that needs to be updated when the {@link #timer}
     * changes its playing state
     */
    private Anchor playPause;

    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final HorizontalPanel filterControlPanel;
    private Label filterStatusLabel;
    private Button filterClearButton;
    
    /**
     * The handler for changes in the leaderboard table's selection; its registration is kept in
     * {@link #leaderboardAsTableSelectionModelRegistration} while it is registered as a selection handler
     * on the {@link #leaderboardTable}.
     */
    private final Handler selectionChangeHandler;
    
    /**
     * While the {@link #selectionChangeHandler} is registered as a selection change handler on the
     * {@link #leaderboardTable}'s selection model, this field holds the registration which can be used to
     * remove the registration again. We'll use this to temporarily suspend selection events when actively
     * modifying / adjusting the table selection to match the {@link #competitorSelectionProvider}.
     */
    private HandlerRegistration leaderboardAsTableSelectionModelRegistration;

    /**
     * If this is <code>null</code>, all leaderboard columns added by updating the leaderboard from the server are
     * automatically added to the table. Otherwise, only the column whose
     * {@link RaceColumnDTO#getRaceIdentifier(String) race identifier} matches the value of this attribute will be
     * added.
     */
    private final RaceIdentifier preSelectedRace;

    private final VerticalPanel contentPanel;
    
    private HorizontalPanel refreshAndSettingsPanel;
    private Label scoreCorrectionLastUpdateTimeLabel;
    private Label scoreCorrectionCommentLabel;
    private Label liveRaceLabel; 
    
    private boolean isEmbedded;

    private static LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);

    private ImageResource pauseIcon;
    private ImageResource playIcon;

    private final BusyIndicator busyIndicator;

    /**
     * Tells whether the leaderboard settings were explicitly changed by an external call to
     * {@link #updateSettings(LeaderboardSettings)}. If so, a {@link #playStateChanged(PlayStates, PlayModes) play state
     * change} will not automatically lead to a settings change.
     */
    private boolean settingsUpdatedExplicitly = false;

    /**
     * Tells if the leaderboard is currently handling a {@link #playStateChanged(PlayStates, PlayModes) play state
     * change}. If this is the case, a call to {@link #updateSettings(LeaderboardSettings)} won't set the
     * {@link #settingsUpdatedExplicitly} flag.
     */
    private boolean currentlyHandlingPlayStateChange;

    private PlayModes oldPlayMode;

    private final AsyncActionsExecutor asyncActionsExecutor;

    /**
     * See also {@link #getDefaultSortColumn()}. If no other column is explicitly selected for sorting and this
     * attribute holds a non-<code>null</code> string identifying a valid race by name that is represented in this
     * leaderboard panel then sort by it. Otherwise, default sorting will default to the overall rank column.
     */
    private String raceNameForDefaultSorting;

    /**
     * Can be used to disallow users to drill into the race details.
     */
    private final boolean showRaceDetails;
    
    /**
     * The {@link LastNRacesColumnSelection} column selection strategy requires a {@link RaceTimesInfoProvider}. This can either be injected
     * by passing a non-<code>null</code> object of that type to the constructor, or such an object is created and remembered in this
     * attribute when required the first time.
     */
    private RaceTimesInfoProvider raceTimesInfoProvider;
    private RaceTimesInfoProviderListener raceTimesInfoProviderListener;
    
    private int blurInOnSelectionChanged;
    
    /**
     * When an element in the leaderboard receives focus, it needs to be blurred again to keep the surrounding scroll panel
     * from scrolling anything into view
     */
    private Element elementToBlur;
    private boolean showSelectionCheckbox;
    
    private List<LeaderboardUpdateListener> leaderboardUpdateListener;

    private boolean initialCompetitorFilterHasBeenApplied = false;
    private final boolean showCompetitorFilterStatus;

    private CompetitorFilterPanel competitorFilterPanel;

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public Widget getHeaderWidget() {
        return null;
    }

    @Override
    public Widget getContentWidget() {
        return contentPanel;
    }

    @Override
    public Widget getToolbarWidget() {
        return null;
    }

    @Override
    public Widget getLegendWidget() {
    	return null;
    }
    
    @Override
    public boolean isEmbedded() {
        return isEmbedded;
    }

    public VerticalPanel getContentPanel() {
        return contentPanel;
    }
    
    protected ImageResource getSettingsIcon() {
        return resources.settingsIcon();
    }

    public void updateSettings(final LeaderboardSettings newSettings) {
        boolean oldShallAddOverallDetails = shallAddOverallDetails();
        if (newSettings.getOverallDetailsToShow() != null) {
            selectedOverallDetailColumns.clear();
            selectedOverallDetailColumns.addAll(newSettings.getOverallDetailsToShow());
        }
        if (!newSettings.isUpdateUponPlayStateChange() || !currentlyHandlingPlayStateChange) {
            settingsUpdatedExplicitly = true;
        }
        setShowAddedScores(newSettings.isShowAddedScores());
        setShowCompetitorSailId(newSettings.isShowCompetitorSailIdColumn());
        setShowCompetitorFullName(newSettings.isShowCompetitorFullNameColumn());
        setShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(newSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor());
        final List<ExpandableSortableColumn<?>> columnsToExpandAgain = new ArrayList<ExpandableSortableColumn<?>>();
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(i);
            if (c instanceof ExpandableSortableColumn<?>) {
                ExpandableSortableColumn<?> expandableSortableColumn = (ExpandableSortableColumn<?>) c;
                if (expandableSortableColumn.isExpanded()) {
                    // now toggle expansion back and forth,
                    // enforcing a re-build of the visible
                    // child columns
                    expandableSortableColumn.toggleExpansion();
                    columnsToExpandAgain.add(expandableSortableColumn);
                }
            }
        }
        if (newSettings.getManeuverDetailsToShow() != null) {
            selectedManeuverDetails.clear();
            selectedManeuverDetails.addAll(newSettings.getManeuverDetailsToShow());
        }
        if (newSettings.getLegDetailsToShow() != null) {
            selectedLegDetails.clear();
            selectedLegDetails.addAll(newSettings.getLegDetailsToShow());
        }
        if (newSettings.getRaceDetailsToShow() != null) {
            selectedRaceDetails.clear();
            selectedRaceDetails.addAll(newSettings.getRaceDetailsToShow());
        }
        // update strategy for determining the race columns to show; if settings' race columns to show is null, use the
        // previously selected race columns / number of race columns for the new configuration
        Iterable<String> oldNamesOfRaceColumnsToShow = null;
        if (newSettings.getNamesOfRaceColumnsToShow() == null) {
            oldNamesOfRaceColumnsToShow = raceColumnSelection.getSelectedRaceColumnNames();
        }
        switch (newSettings.getActiveRaceColumnSelectionStrategy()) {
        case EXPLICIT:
            if (preSelectedRace == null) {
                raceColumnSelection = new ExplicitRaceColumnSelection();
            } else {
                raceColumnSelection = new ExplicitRaceColumnSelectionWithPreselectedRace(preSelectedRace);
            }
            if (newSettings.getNamesOfRaceColumnsToShow() != null) {
                raceColumnSelection.requestClear();
                for (String nameOfRaceColumnToShow : newSettings.getNamesOfRaceColumnsToShow()) {
                    RaceColumnDTO raceColumnToShow = getRaceByColumnName(nameOfRaceColumnToShow);
                    if (raceColumnToShow != null) {
                        raceColumnSelection.requestRaceColumnSelection(raceColumnToShow);
                    }
                }
            } else {
                // apply the old column selections again
                for (String oldNameOfRaceColumnToShow : oldNamesOfRaceColumnsToShow) {
                    final RaceColumnDTO raceColumnByName = getLeaderboard().getRaceColumnByName(oldNameOfRaceColumnToShow);
                    if (raceColumnByName != null) {
                        raceColumnSelection.requestRaceColumnSelection(raceColumnByName);
                    }
                }
                if (newSettings.getNamesOfRacesToShow() != null) {
                    raceColumnSelection.requestClear();
                    for (String nameOfRaceToShow : newSettings.getNamesOfRacesToShow()) {
                        RaceColumnDTO raceColumnToShow = getRaceByName(nameOfRaceToShow);
                        if (raceColumnToShow != null) {
                            raceColumnSelection.requestRaceColumnSelection(raceColumnToShow);
                        }
                    }
                }
            }
            break;
        case LAST_N:
            setRaceColumnSelectionToLastNStrategy(newSettings.getNumberOfLastRacesToShow());
            break;
        }
        
        final boolean oldBusyState = getBusyIndicator().isBusy(); 
        getBusyIndicator().setBusy(true);
        Runnable doWhenNecessaryDetailHasBeenLoaded = new Runnable() {
            @Override
            public void run() {
                setAutoExpandPreSelectedRace(false); // avoid expansion during updateLeaderboard(...); will expand later
                                                     // if it was expanded before
                // update leaderboard after settings panel column selection change
                updateLeaderboard(leaderboard);
                setAutoExpandPreSelectedRace(newSettings.isAutoExpandPreSelectedRace());

                if (newSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
                    timer.setRefreshInterval(newSettings.getDelayBetweenAutoAdvancesInMilliseconds());
                }
                for (ExpandableSortableColumn<?> expandableSortableColumn : columnsToExpandAgain) {
                    expandableSortableColumn.toggleExpansion();
                }
                if (newSettings.getNameOfRaceToSort() != null) {
                    final RaceColumn<?> raceColumnByRaceName = getRaceColumnByRaceName(newSettings
                            .getNameOfRaceToSort());
                    if (raceColumnByRaceName != null) {
                        getLeaderboardTable().sortColumn(raceColumnByRaceName, /* ascending */true);
                    }
                }
                getBusyIndicator().setBusy(oldBusyState);
            }
        };
        if (oldShallAddOverallDetails == shallAddOverallDetails() || oldShallAddOverallDetails || getLeaderboard().hasOverallDetails()) {
            doWhenNecessaryDetailHasBeenLoaded.run();
        } else { // meaning that now the details need to be loaded from the server
            updateLeaderboardAndRun(doWhenNecessaryDetailHasBeenLoaded);
        }
    }

    /**
     * @param callWhenExpansionDataIsLoaded
     */
    private void updateLeaderboardAndRun(final Runnable callWhenExpansionDataIsLoaded) {
        final LeaderboardDTO previousLeaderboard = getLeaderboard();
        getSailingService().getLeaderboardByName(getLeaderboardName(),
                timer.getPlayMode() == PlayModes.Live ? null : getLeaderboardDisplayDate(),
                /* namesOfRacesForWhichToLoadLegDetails */getNamesOfExpandedRaces(),
                shallAddOverallDetails(), previousLeaderboard.getId(), /* fillNetPointsUncorrected */ false,
                new MarkedAsyncCallback<IncrementalOrFullLeaderboardDTO>(
                        new AsyncCallback<IncrementalOrFullLeaderboardDTO>() {
                            @Override
                            public void onSuccess(IncrementalOrFullLeaderboardDTO result) {
                                updateLeaderboard(result.getLeaderboardDTO(previousLeaderboard));
                                callWhenExpansionDataIsLoaded.run();
                            }
  
                            @Override
                            public void onFailure(Throwable caught) {
                                getErrorReporter().reportError(
                                        stringMessages.errorTryingToObtainLeaderboardContents(caught.getMessage()),
                                        true /* silentMode */);
                            }
                        }));
    }

    void setRaceColumnSelectionToLastNStrategy(final Integer numberOfLastRacesToShow) {
        raceColumnSelection = new LastNRacesColumnSelection(numberOfLastRacesToShow, getRaceTimesInfoProvider());
        if (timer.getPlayState() != Timer.PlayStates.Playing) {
            // wait for the first update and adjust leaderboard once the race times have been received
            raceTimesInfoProviderListener = new RaceTimesInfoProviderListener() {
                @Override
                public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
                    // remove 
                    timer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
                    // remove the listener only in case a leaderboard has already been loaded
                    if(getLeaderboard() != null) {
                        updateLeaderboard(getLeaderboard());
                        getRaceTimesInfoProvider().removeRaceTimesInfoProviderListener(raceTimesInfoProviderListener);
                        raceTimesInfoProviderListener = null;
                    }
                }
            };
            getRaceTimesInfoProvider().addRaceTimesInfoProviderListener(raceTimesInfoProviderListener);
        }
    }

    /**
     * A leaderboard panel may have been provided with a valid {@link RaceTimesInfoProvider} upon creation; in this case, that object
     * will be returned. If none was provided to the constructor, one is created and remembered if no previously created/remembered
     * object exists.<p>
     * 
     * Precondition: {@link #timer} is not <code>null</code>
     */
    private RaceTimesInfoProvider getRaceTimesInfoProvider() {
        if (raceTimesInfoProvider == null) {
            final List<RegattaAndRaceIdentifier> trackedRacesIdentifiers;
            if (leaderboard != null && getTrackedRacesIdentifiers() != null) {
                trackedRacesIdentifiers = getTrackedRacesIdentifiers();
            } else {
                trackedRacesIdentifiers = Collections.emptyList();
            }
            raceTimesInfoProvider = new RaceTimesInfoProvider(getSailingService(), asyncActionsExecutor, errorReporter,
                    trackedRacesIdentifiers, timer.getRefreshInterval());
        }
        return raceTimesInfoProvider;
    }

    protected class CompetitorColumn extends LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, LeaderboardRowDTO> {
        private final CompetitorColumnBase<LeaderboardRowDTO> base;
        
        protected CompetitorColumn(CompetitorColumnBase<LeaderboardRowDTO> base) {
            super(base.getCell(getLeaderboard()), SortingOrder.ASCENDING, LeaderboardPanel.this);
            this.base = base;
        }

        public CompetitorColumn(CompositeCell<LeaderboardRowDTO> compositeCell, CompetitorColumnBase<LeaderboardRowDTO> base) {
            super(compositeCell, SortingOrder.ASCENDING, LeaderboardPanel.this);
            this.base = base;
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    return Collator.getInstance().compare(getLeaderboard().getDisplayName(o1.competitor),
                            getLeaderboard().getDisplayName(o2.competitor));
                }
            };
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return base.getHeader();
        }

        @Override
        public LeaderboardRowDTO getValue(LeaderboardRowDTO object) {
            return object;
        }

        protected void defaultRender(Context context, LeaderboardRowDTO object, SafeHtmlBuilder sb) {
            super.render(context, object, sb);
        }
        
        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder sb) {
            String competitorColor = LeaderboardPanel.this.competitorSelectionProvider.getColor(object.competitor).getAsHtml();
            String competitorColorBarStyle;
            if (LeaderboardPanel.this.isEmbedded && preSelectedRace != null) {
                competitorColorBarStyle = "style=\"border-bottom: 2px solid " + competitorColor + ";\"";
            } else {
                competitorColorBarStyle = "style=\"border: none;\"";
            }
            base.render(object, competitorColorBarStyle, sb);
        }
    }

    /**
     * Shows the country flag and sail ID, if present
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    private class SailIDColumn<T> extends LeaderboardSortableColumnWithMinMax<T, String> {
        private final CompetitorFetcher<T> competitorFetcher;
        
        protected SailIDColumn(CompetitorFetcher<T> competitorFetcher) {
            super(new TextCell(), SortingOrder.ASCENDING, LeaderboardPanel.this);
            this.competitorFetcher = competitorFetcher;
        }

        @Override
        public InvertibleComparator<T> getComparator() {
            return new InvertibleComparatorAdapter<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return competitorFetcher.getCompetitor(o1).getSailID() == null ? competitorFetcher.getCompetitor(o2).getSailID() == null ? 0 : -1
                            : competitorFetcher.getCompetitor(o2).getSailID() == null ? 1 : Collator.getInstance().compare(
                                    competitorFetcher.getCompetitor(o1).getSailID(), competitorFetcher.getCompetitor(o2).getSailID());
                }
            };
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.competitor()), stringMessages.sailIdColumnTooltip());
        }

        @Override
        public void render(Context context, T object, SafeHtmlBuilder sb) {
            ImageResourceRenderer renderer = new ImageResourceRenderer();
            final String twoLetterIsoCountryCode = competitorFetcher.getCompetitor(object).getTwoLetterIsoCountryCode();
            final ImageResource flagImageResource;
            if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                flagImageResource = FlagImageResolver.getEmptyFlagImageResource();
            } else {
                flagImageResource = FlagImageResolver.getFlagImageResource(twoLetterIsoCountryCode);
            }
            if (flagImageResource != null) {
                sb.append(renderer.render(flagImageResource));
                sb.appendHtmlConstant("&nbsp;");
            }
            sb.appendEscaped(competitorFetcher.getCompetitor(object).getSailID());
        }

        @Override
        public String getValue(T object) {
            return competitorFetcher.getCompetitor(object).getSailID();
        }
    }

    /**
     * Displays net/total points and possible max-points reasons based on a {@link LeaderboardRowDTO} and a race name
     * and makes the column sortable by the total points.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    protected abstract class RaceColumn<C> extends ExpandableSortableColumn<C> {
        private RaceColumnDTO race;

        private final String headerStyle;
        private final String columnStyle;

        public RaceColumn(RaceColumnDTO race, boolean enableExpansion, Cell<C> cell,
                SortingOrder preferredSortingOrder, String headerStyle, String columnStyle) {
            super(LeaderboardPanel.this, enableExpansion, cell, preferredSortingOrder, stringMessages,
                    LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, selectedRaceDetails, LeaderboardPanel.this);
            setHorizontalAlignment(ALIGN_CENTER);
            this.race = race;
            this.headerStyle = headerStyle;
            this.columnStyle = columnStyle;
        }

        public RaceColumnDTO getRace() {
            return race;
        }
        
        public void setRace(RaceColumnDTO race) {
        	this.race = race;
        }

        public String getRaceColumnName() {
            return race.getRaceColumnName();
        }

        public boolean isMedalRace() {
            return race.isMedalRace();
        }

        public boolean isLive(FleetDTO fleetDTO) {
            return race.isLive(fleetDTO, timer.getLiveTimePointInMillis());
        }
        
        @Override
        public String getColumnStyle() {
            return columnStyle;
        }
        
        /**
         * Computes added scores for this RaceColumn
         */
        private double computeAddedScores(LeaderboardRowDTO object) {
            double addedScores = 0;
            for (RaceColumnDTO raceColumn : getLeaderboard().getRaceList()) {
                LeaderboardEntryDTO entryBefore = object.fieldsByRaceColumnName.get(raceColumn.getName());
                if (entryBefore.totalPoints != null) {
                    addedScores += entryBefore.totalPoints;
                } 
                if (raceColumn.getName().equals(getRaceColumnName())) {
                    break; // we've reached the current column - stop here
                }
            }
            return addedScores;
        }

        /**
         * Displays a combination of total points and maxPointsReason in bold, transparent, strike-through, depending on
         * various criteria. Here's how:
         * 
         * <pre>
         *                                  total points                |    maxPointsReason
         * -------------------------------+-----------------------------+-----------------------
         *  not discarded, no maxPoints   | bold                        | none
         *  not discarded, maxPoints      | bold                        | transparent
         *  discarded, no maxPoints       | transparent, strike-through | none
         *  discarded, maxPoints          | transparent, strike-through | transparent, strike-through
         * </pre>
         */
        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder html) {
            LeaderboardEntryDTO entry = object.fieldsByRaceColumnName.get(getRaceColumnName());
            if (entry != null) {
            	boolean isLive = isLive(entry.fleet);
            	
                String textColor = isLive ? IS_LIVE_TEXT_COLOR : DEFAULT_TEXT_COLOR;
                String totalOrAddedPointsAsText = isShowAddedScores() ? (entry.totalPoints != null ? scoreFormat.format(computeAddedScores(object)) : "")
                        : entry.totalPoints == null ? "" : scoreFormat.format(entry.totalPoints);
                String netOrAddedPointsAsText = isShowAddedScores() ? (entry.netPoints != null ? scoreFormat.format(computeAddedScores(object)) : "")
                        : entry.netPoints == null ? "" : scoreFormat.format(entry.netPoints);
                
                if (entry.fleet != null && entry.fleet.getColor() != null) {
                    html.append(raceColumnTemplate.cellFrameWithTextColorAndFleetBorder(textColor, entry.fleet.getColor().getAsHtml()));
                } else {
                    html.append(raceColumnTemplate.cellFrameWithTextColor(textColor));
                }
                
                // don't show points if max points / penalty
                if (entry.reasonForMaxPoints == null || entry.reasonForMaxPoints == MaxPointsReason.NONE) {
                    if (!entry.discarded) {
                        html.appendHtmlConstant("<span style=\"font-weight: bold;\">");
                        html.appendHtmlConstant(totalOrAddedPointsAsText);
                        html.appendHtmlConstant("</span>");
                    } else {
                        html.appendHtmlConstant(" <span style=\"opacity: 0.5;\"><del>");
                        html.appendHtmlConstant(netOrAddedPointsAsText);
                        html.appendHtmlConstant("</del></span>");
                    }
                } else {
                    html.appendHtmlConstant(" <span title=\"" + netOrAddedPointsAsText + "/" + totalOrAddedPointsAsText
                            + "\" style=\"opacity: 0.5;\">");
                    if (entry.discarded) {
                        html.appendHtmlConstant("<del>");
                    }
                    html.appendEscaped(entry.reasonForMaxPoints == MaxPointsReason.NONE ? "" : entry.reasonForMaxPoints.name());
                    if (entry.discarded) {
                        html.appendHtmlConstant("</del>");
                    }
                    html.appendHtmlConstant("</span>");
                }
                html.appendHtmlConstant("</div>");
            }
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    if (isShowAddedScores()) {
                        double o1AddedScore = computeAddedScores(o1);
                        double o2AddedScore = computeAddedScores(o2);
                        // FIXME bug 2717: need to respect the scoring scheme's isHigherBetter() flag here
                        double result = o1AddedScore == 0. ? o2AddedScore == 0. ? 0. : isAscending() ? 1. : -1. : o2AddedScore == 0. ? isAscending() ? 1. : -1. : o2AddedScore - o1AddedScore;
                        return result > 0 ? 1 : result < 0 ? -1 : 0;
                    } else {
                        List<CompetitorDTO> competitorsFromBestToWorst = getLeaderboard().getCompetitorsFromBestToWorst(
                                race);
                        int o1Rank = competitorsFromBestToWorst.indexOf(o1.competitor) + 1;
                        int o2Rank = competitorsFromBestToWorst.indexOf(o2.competitor) + 1;
                        return o1Rank == 0 ? o2Rank == 0 ? 0 : isAscending() ? 1 : -1 : o2Rank == 0 ? isAscending() ? -1
                                : 1 : o1Rank - o2Rank;
                    }
                }
            };
        }

        @Override
        public String getHeaderStyle() {
            return headerStyle;
        }

        @Override
        public Header<SafeHtml> getHeader() {
            SortableExpandableColumnHeader header = new SortableExpandableColumnHeader(/* title */race.getRaceColumnName(),
            /* iconURL */race.isMedalRace() ? "/gwt/images/medal_small.png" : null, LeaderboardPanel.this, this, stringMessages);
            return header;
        }
    }

    public static DetailType[] getAvailableRaceDetailColumnTypes() {
        return new DetailType[] { DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_DISTANCE_TRAVELED, DetailType.RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START,
                DetailType.RACE_TIME_TRAVELED,
                DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS, DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS,
                DetailType.NUMBER_OF_MANEUVERS, DetailType.DISPLAY_LEGS, DetailType.CURRENT_LEG,
                DetailType.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS,
                DetailType.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS,
                DetailType.RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START, DetailType.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START,
                DetailType.DISTANCE_TO_START_AT_RACE_START, DetailType.TIME_BETWEEN_RACE_START_AND_COMPETITOR_START,
                DetailType.SPEED_OVER_GROUND_AT_RACE_START,
                DetailType.SPEED_OVER_GROUND_WHEN_PASSING_START,
                DetailType.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS,
                DetailType.START_TACK,
                DetailType.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL };
    }

    public static DetailType[] getAvailableOverallDetailColumnTypes() {
        return new DetailType[] { DetailType.REGATTA_RANK,
                                  DetailType.TOTAL_DISTANCE_TRAVELED,
                                  DetailType.TOTAL_AVERAGE_SPEED_OVER_GROUND,
                                  DetailType.TOTAL_TIME_SAILED_IN_SECONDS,
                                  DetailType.MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS };
    }

    private class TextRaceColumn extends RaceColumn<String> implements RaceNameProvider {
        /**
         * Remembers the leg columns; <code>null</code>-padded, if {@link #getLegColumn(int)} asks for a column index
         * not yet existing. It is important to remember the columns because column removal happens based on identity.
         */
        private final List<LegColumn> legColumns;

        public TextRaceColumn(RaceColumnDTO race, boolean expandable, SortingOrder preferredSortingOrder,
                String headerStyle, String columnStyle) {
            super(race, expandable, new TextCell(), preferredSortingOrder, headerStyle, columnStyle);
            legColumns = new ArrayList<LegColumn>();
        }

        public String getValue(LeaderboardRowDTO object) {
            // The following code exists only for robustness. This method should never be called because
            // RaceColumn implements its own render(...) method which doesn't make use of getValue(...)
            final Double totalPoints = object.fieldsByRaceColumnName.get(getRaceColumnName()).totalPoints;
            return "" + (totalPoints == null ? "" : scoreFormat.format(totalPoints));
        }

        @Override
        protected void ensureExpansionDataIsLoaded(final Runnable callWhenExpansionDataIsLoaded) {
            if (getLeaderboard().getLegCount(getRaceColumnName(), preSelectedRace) != -1) {
                callWhenExpansionDataIsLoaded.run();
            } else {
                updateLeaderboardAndRun(callWhenExpansionDataIsLoaded);
            }
        }

        @Override
        protected Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDetailColumnMap(
                LeaderboardPanel leaderboardPanel, StringMessages stringMessages, String detailHeaderStyle,
                String detailColumnStyle) {
            Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result = new HashMap<>();
            result.put(DetailType.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL,
                    new TimeSinceLastGpsFixColumn(DetailType.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL, 
                            new RaceRatioBetweenTimeSinceLastPositionFixAndAverageSamplingInterval(), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_DISTANCE_TRAVELED,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_DISTANCE_TRAVELED, new RaceDistanceTraveledInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_DISTANCE_TRAVELED_INCLUDING_GATE_START, new RaceDistanceTraveledIncludingGateStartInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, 
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, new RaceAverageSpeedInKnots(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_GAP_TO_LEADER_IN_SECONDS, new RaceGapToLeaderInSeconds(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS, new CurrentSpeedOverGroundInKnots(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS, new RaceDistanceToLeaderInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS, new RaceAverageAbsoluteCrossTrackErrorInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS, new RaceAverageSignedCrossTrackErrorInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START, new DistanceToStartFiveSecondsBeforeStartInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START, new SpeedFiveSecondsBeforeStartInKnots(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.DISTANCE_TO_START_AT_RACE_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.DISTANCE_TO_START_AT_RACE_START, new DistanceToStartAtRaceStartInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.TIME_BETWEEN_RACE_START_AND_COMPETITOR_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.TIME_BETWEEN_RACE_START_AND_COMPETITOR_START, new TimeBetweenRaceStartAndCompetitorStartInSeconds(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.SPEED_OVER_GROUND_AT_RACE_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.SPEED_OVER_GROUND_AT_RACE_START, new SpeedOverGroundAtRaceStartInKnots(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.SPEED_OVER_GROUND_WHEN_PASSING_START,
                    new FormattedDoubleDetailTypeColumn(DetailType.SPEED_OVER_GROUND_WHEN_PASSING_START, new SpeedOverGroundWhenPassingStartInKnots(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS,
                    new FormattedDoubleDetailTypeColumn(DetailType.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS, new DistanceToStarboardSideOfStartLineInMeters(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.START_TACK, new StartingTackColumn(new TackWhenStarting(), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(DetailType.NUMBER_OF_MANEUVERS, getManeuverCountRaceColumn());
            result.put(DetailType.CURRENT_LEG,
                    new FormattedDoubleDetailTypeColumn(DetailType.CURRENT_LEG, new CurrentLeg(), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, LeaderboardPanel.this));
            result.put(DetailType.RACE_TIME_TRAVELED, getTimeTraveledRaceColumn());

            return result;
        }

        private TimeTraveledRaceColumn getTimeTraveledRaceColumn() {
            return new TimeTraveledRaceColumn(getLeaderboardPanel(), this, stringMessages, LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE,
                    LEG_DETAIL_COLUMN_HEADER_STYLE, LEG_DETAIL_COLUMN_STYLE);
        }

        private ManeuverCountRaceColumn getManeuverCountRaceColumn() {
            return new ManeuverCountRaceColumn(getLeaderboardPanel(), this, stringMessages,
                    LeaderboardPanel.this.selectedManeuverDetails, LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE,
                    LEG_DETAIL_COLUMN_HEADER_STYLE, LEG_DETAIL_COLUMN_STYLE, LeaderboardPanel.this);
        }

        @Override
        protected Iterable<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDirectChildren() {
            List<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result = new ArrayList<>();
            for (AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> column : super.getDirectChildren()) {
                result.add(column);
            }
            if (isExpanded() && selectedRaceDetails.contains(DetailType.DISPLAY_LEGS)) {
                // it is important to re-use existing LegColumn objects because
                // removing the columns from the table
                // is based on column identity
                int maxLegCount = getLeaderboard().getLegCount(getRaceColumnName(), preSelectedRace);
                if (maxLegCount != -1) {
                    for (int i = 0; i < maxLegCount; i++) {
                        LegColumn legColumn = getLegColumn(i);
                        result.add(legColumn);
                    }
                } else {
                    // the race is no longer part of the LeaderboardDTO; consider the non-null legs in legColumns:
                    for (LegColumn legColumn : legColumns) {
                        if (legColumn != null) {
                            result.add(legColumn);
                        }
                    }
                }
            }
            return result;
        }

        private LegColumn getLegColumn(int legNumber) {
            LegColumn result;
            if (legColumns.size() > legNumber && legColumns.get(legNumber) != null) {
                result = legColumns.get(legNumber);
            } else {
                result = new LegColumn(LeaderboardPanel.this, getRaceColumnName(), legNumber, SortingOrder.ASCENDING,
                        stringMessages, Collections.unmodifiableList(selectedLegDetails), LEG_COLUMN_HEADER_STYLE,
                        LEG_COLUMN_STYLE, LEG_DETAIL_COLUMN_HEADER_STYLE, LEG_DETAIL_COLUMN_STYLE);
                while (legColumns.size() <= legNumber) {
                    legColumns.add(null);
                }
                legColumns.set(legNumber, result);
            }
            return result;
        }

        /**
         * Reports the ratio of the time that passed since the last position fix and the average sampling interval of the
         * competitor's track. On a perfect track, this value will never exceed 1.0. Immediately when a fix is received, this
         * value goes to 0.0. For a competitor whose tracker is lagging, this value can grow considerably greater than 1.
         * The value goes to <code>null</code> if there are no fixes in the track.
         * 
         * @author Axel Uhl (D043530)
         *
         */
        private class RaceRatioBetweenTimeSinceLastPositionFixAndAverageSamplingInterval implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.timeSinceLastPositionFixInSeconds != null && fieldsForRace.averageSamplingInterval != null) {
                    result = fieldsForRace.timeSinceLastPositionFixInSeconds / fieldsForRace.averageSamplingInterval.asSeconds();
                }
                return result;
            }
        }

        /**
         * Accumulates the average speed over all legs of a race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceAverageSpeedInKnots implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    double distanceTraveledInMeters = 0;
                    long timeInMilliseconds = 0;
                    for (LegEntryDTO legDetail : fieldsForRace.legDetails) {
                        if (legDetail != null) {
                            if (legDetail.distanceTraveledInMeters != null && legDetail.timeInMilliseconds != null) {
                                distanceTraveledInMeters += legDetail.distanceTraveledInMeters;
                                timeInMilliseconds += legDetail.timeInMilliseconds;
                            } else {
                                distanceTraveledInMeters = 0;
                                timeInMilliseconds = 0;
                                break;
                            }
                        }
                    }
                    if (timeInMilliseconds != 0) {
                        result = distanceTraveledInMeters / (double) timeInMilliseconds * 1000 * 3600 / 1852;
                    }
                }
                return result;
            }
        }

        /**
         * Fetches the average absolute (distance always counted as positive, no matter whether left or right) cross-track error for the race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceAverageAbsoluteCrossTrackErrorInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.averageAbsoluteCrossTrackErrorInMeters;
                }
                return result;
            }
        }

        /**
         * Fetches the average signed (right course side is positive, left course side is negative) cross-track error for the race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceAverageSignedCrossTrackErrorInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.averageSignedCrossTrackErrorInMeters;
                }
                return result;
            }
        }

        /**
         * Fetches the competitor's distance to the start mark at the time the race started
         * 
         * @author Axel Uhl (D043530)
         */
        private class DistanceToStartAtRaceStartInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.distanceToStartLineAtStartOfRaceInMeters;
                }
                return result;
            }
        }

        /**
         * Fetches the time between start of race and the competitors start mark passing, telling
         * how long after the gun the competitor actually started.
         * 
         * @author Axel Uhl (D043530)
         */
        private class TimeBetweenRaceStartAndCompetitorStartInSeconds implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.timeBetweenRaceStartAndCompetitorStartInSeconds;
                }
                return result;
            }
        }

        /**
         * Fetches the competitor's speed over ground at the time the race started
         * 
         * @author Axel Uhl (D043530)
         */
        private class SpeedOverGroundAtRaceStartInKnots implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.speedOverGroundAtStartOfRaceInKnots;
                }
                return result;
            }
        }

        /**
         * Fetches the competitor's distance to the start mark five seconds before the race started
         */
        private class DistanceToStartFiveSecondsBeforeStartInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.distanceToStartLineFiveSecondsBeforeStartInMeters;
                }
                return result;
            }
        }

        /**
         * Fetches the competitor's speed over ground five seconds before the race started
         */
        private class SpeedFiveSecondsBeforeStartInKnots implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.speedOverGroundFiveSecondsBeforeStartInKnots;
                }
                return result;
            }
        }

        /**
         * Fetches the competitor's speed over ground at the time the competitor passed the start
         * 
         * @author Axel Uhl (D043530)
         */
        private class SpeedOverGroundWhenPassingStartInKnots implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.speedOverGroundAtPassingStartWaypointInKnots;
                }
                return result;
            }
        }
        
        /**
         * Fetches the competitor's distance to the starboard side of the start line when competitor passed the start.
         * If the start waypoint is not a gate/line, the distance to the single buoy is used.
         * 
         * @author Axel Uhl (D043530)
         */
        private class DistanceToStarboardSideOfStartLineInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.distanceToStarboardSideOfStartLineInMeters;
                }
                return result;
            }
        }
        
        /**
         * Fetches the competitor's speed over ground at the time the competitor passed the start
         * 
         * @author Axel Uhl (D043530)
         */
        private class TackWhenStarting implements LegDetailField<Tack> {
            @Override
            public Tack get(LeaderboardRowDTO row) {
                Tack result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.startTack;
                }
                return result;
            }
        }

        /**
         * Accumulates the distance traveled over all legs of a race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceDistanceTraveledInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    for (LegEntryDTO legDetail : fieldsForRace.legDetails) {
                        if (legDetail != null) {
                            if (legDetail.distanceTraveledInMeters != null) {
                                if (result == null) {
                                    result = 0.0;
                                }
                                result += legDetail.distanceTraveledInMeters;
                            }
                        }
                    }
                }
                return result;
            }
        }

        /**
         * Accumulates the distance traveled over all legs of a race and considers the specifics of a gate start. The
         * first leg of a gate start gets as additional distance traveled the distance from the port side of the line to
         * the point where the competitor started which helps normalize the distance traveled and make them comparable
         * across early and late starters.
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceDistanceTraveledIncludingGateStartInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    for (LegEntryDTO legDetail : fieldsForRace.legDetails) {
                        if (legDetail != null) {
                            if (legDetail.distanceTraveledIncludingGateStartInMeters != null) {
                                if (result == null) {
                                    result = 0.0;
                                }
                                result += legDetail.distanceTraveledIncludingGateStartInMeters;
                            }
                        }
                    }
                }
                return result;
            }
        }

        private class CurrentLeg implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null && !fieldsForRace.legDetails.isEmpty()) {
                    for (LegEntryDTO legDetail : fieldsForRace.legDetails) {
                        if (legDetail != null) {
                            if (legDetail.started) {
                                if (result == null) {
                                    result = 0.0;
                                }
                                result++;
                            } else {
                                if (result != null) {
                                    result++;
                                }
                                break;
                            }
                        }
                    }
                }
                return result;
            }
        }

        /**
         * Computes the gap to leader exploiting the ordering of the leg detail columns
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceGapToLeaderInSeconds implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    int lastLegIndex = fieldsForRace.legDetails.size() - 1;
                    LegEntryDTO lastLegDetail = fieldsForRace.legDetails.get(lastLegIndex);
                    // competitor may be in leg prior to the one the leader is in; find competitors current leg
                    while (lastLegDetail == null && lastLegIndex > 0) {
                        lastLegDetail = fieldsForRace.legDetails.get(--lastLegIndex);
                    }
                    if (lastLegDetail != null) {
                        result = lastLegDetail.gapToLeaderInSeconds;
                    }
                }
                return result;
            }
        }

        /**
         * Computes the windward distance to the overall leader in meters
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceDistanceToLeaderInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.windwardDistanceToOverallLeaderInMeters != null) {
                    result = fieldsForRace.windwardDistanceToOverallLeaderInMeters;
                }
                return result;
            }
        }
        
        private class CurrentSpeedOverGroundInKnots implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    int lastLegIndex = fieldsForRace.legDetails.size() - 1;
                    if (lastLegIndex >= 0) {
                        LegEntryDTO lastLegDetail = fieldsForRace.legDetails.get(lastLegIndex);
                        // competitor may be in leg prior to the one the leader is in; find competitors current leg
                        while (lastLegDetail == null && lastLegIndex > 0) {
                            lastLegDetail = fieldsForRace.legDetails.get(--lastLegIndex);
                        }
                        if (lastLegDetail != null) {
                            result = lastLegDetail.currentSpeedOverGroundInKnots;
                        }
                    }
                }
                return result;
            }
        }
    }
    
    /**
     * Column that display the number of races a competitor has finished. Currently
     * taking into account {@link MaxPointsReason#DNS}, {@link MaxPointsReason#DNF}
     * and {@link MaxPointsReason#DNC}. Configurable over {@link LeaderboardSettings#isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor()}.
     * 
     * @author Simon Marcel Pamies
     */
    private class TotalRacesCompletedColumn extends LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, String> {
        private final String columnStyle;
        private final MaxPointsReason[] MAX_POINTS_REASONS_THAT_IDENTIFY_NON_FINISHED_RACES = 
                new MaxPointsReason[] {MaxPointsReason.DNS, MaxPointsReason.DNF, MaxPointsReason.DNC};

        protected TotalRacesCompletedColumn(String columnStyle) {
            super(new TextCell(), SortingOrder.ASCENDING, LeaderboardPanel.this);
            this.columnStyle = columnStyle;
            setHorizontalAlignment(ALIGN_CENTER);
        }
        
        private int computeNumberOfRacesCompleted(LeaderboardRowDTO object) {
            int racesSailedInRow = 0;
            for (RaceColumnDTO raceColumn : getLeaderboard().getRaceList()) {
                LeaderboardEntryDTO entryBefore = object.fieldsByRaceColumnName.get(raceColumn.getName());
                if (entryBefore.totalPoints != null) {
                    if (entryBefore.reasonForMaxPoints.equals(MaxPointsReason.NONE) ||
                            !Util.contains(Arrays.asList(MAX_POINTS_REASONS_THAT_IDENTIFY_NON_FINISHED_RACES), entryBefore.reasonForMaxPoints)) {
                        racesSailedInRow++;
                    }
                } 
            }
            return racesSailedInRow;
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            return "" + computeNumberOfRacesCompleted(object);
        }

        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder sb) {
            String textColor = getLeaderboard().hasLiveRace(timer.getLiveTimePointInMillis()) ? IS_LIVE_TEXT_COLOR : DEFAULT_TEXT_COLOR;
                
            sb.appendHtmlConstant("<span style=\"font-weight: bold; color:" + textColor + "\">");
            sb.appendEscaped(getValue(object));
            sb.appendHtmlConstant("</span>");
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    double o1RacesSailed = computeNumberOfRacesCompleted(o1);
                    double o2RacesSailed = computeNumberOfRacesCompleted(o2);
                    double result = o1RacesSailed == 0. ? o2RacesSailed == 0. ? 0. : isAscending() ? 1. : -1. : o2RacesSailed == 0. ? isAscending() ? 1. : -1. : o2RacesSailed - o1RacesSailed;
                    return result > 0 ? 1 : result < 0 ? -1 : 0;
                }
            };
        }

        @Override
        public String getColumnStyle() {
            return columnStyle;
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.racesScored()), stringMessages.racesScoredTooltip());
        }
    }

    /**
     * Displays the totals for a competitor for the entire leaderboard.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    private class TotalsColumn extends LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, String> {
        private final String columnStyle;

        protected TotalsColumn(String columnStyle) {
            super(new TextCell(), SortingOrder.ASCENDING, LeaderboardPanel.this);
            this.columnStyle = columnStyle;
            setHorizontalAlignment(ALIGN_CENTER);
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            Double totalPoints = object.totalPoints;
            return "" + (totalPoints == null ? "" : scoreFormat.format(totalPoints));
        }

        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder sb) {
            String textColor = getLeaderboard().hasLiveRace(timer.getLiveTimePointInMillis()) ? IS_LIVE_TEXT_COLOR : DEFAULT_TEXT_COLOR;
        	
            sb.appendHtmlConstant("<span style=\"font-weight: bold; color:" + textColor + "\">");
            sb.appendEscaped(getValue(object));
            sb.appendHtmlConstant("</span>");
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    return getLeaderboard().competitors.indexOf(o1.competitor)
                            - getLeaderboard().competitors.indexOf(o2.competitor);
                }
            };
        }

        @Override
        public String getColumnStyle() {
            return columnStyle;
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.total()), stringMessages.totalsColumnTooltip());
        }
    }

    protected class CarryColumn extends LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, LeaderboardRowDTO> {
        public CarryColumn() {
            super(new AbstractSafeHtmlCell<LeaderboardRowDTO>(new AbstractSafeHtmlRenderer<LeaderboardRowDTO>() {
                @Override
                public SafeHtml render(LeaderboardRowDTO object) {
                    return new SafeHtmlBuilder().appendEscaped(object.carriedPoints == null ? "" : scoreFormat.format(object.carriedPoints)).toSafeHtml();
                }
            }) {
                @Override
                protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
                    sb.append(data);
                }
            }, SortingOrder.ASCENDING, LeaderboardPanel.this);
            setSortable(true);
        }

        protected CarryColumn(Cell<LeaderboardRowDTO> cell) {
            super(cell, SortingOrder.ASCENDING, LeaderboardPanel.this);
            setSortable(true);
        }

        @Override
        public LeaderboardRowDTO getValue(LeaderboardRowDTO object) {
            return object;
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    Double o1CarriedPoints = o1.carriedPoints;
                    if (o1CarriedPoints == null) {
                        o1CarriedPoints = 0.0;
                    }
                    Double o2CarriedPoints = o2.carriedPoints;
                    if (o2CarriedPoints == null) {
                        o2CarriedPoints = 0.0;
                    }
                    return o1CarriedPoints.compareTo(o2CarriedPoints);
                }
            };
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.carry()), stringMessages.carryColumnTooltip());
        }
    }

    private class LeaderboardSelectionCheckboxColumn extends com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn<LeaderboardRowDTO>
    implements CompetitorSelectionChangeListener {
        protected LeaderboardSelectionCheckboxColumn(final CompetitorSelectionProvider competitorSelectionProvider) {
            super(tableResources.cellTableStyle().cellTableCheckboxSelected(), tableResources.cellTableStyle().cellTableCheckboxDeselected(), tableResources.cellTableStyle().cellTableCheckboxColumnCell());
            competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
        }
        
        @Override
        public Boolean getValue(LeaderboardRowDTO row) {
            return competitorSelectionProvider.isSelected(row.competitor);
        }

        @Override public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {}
        @Override public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet, FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {}
        @Override public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {}

        /**
         * Ensure that the checkbox is redrawn when the competitor selection changes
         */
        @Override
        public void addedToSelection(CompetitorDTO competitor) {
            final LeaderboardRowDTO row = getRow(competitor);
            if (row != null) {
                redrawRow(row);
            }
        }

        private void redrawRow(final LeaderboardRowDTO row) {
            final List<LeaderboardRowDTO> leaderboardDataList = getData().getList();
            synchronized (leaderboardDataList) {
                redrawRow(row, leaderboardDataList);
            }
        }

        /**
         * Ensure that the checkbox is redrawn when the competitor selection changes
         */
        @Override
        public void removedFromSelection(CompetitorDTO competitor) {
            final LeaderboardRowDTO row = getRow(competitor);
            if (row != null) {
                redrawRow(row);
            }
        }

        @Override
        public void updateMinMax() {}

        @Override
        protected ListDataProvider<LeaderboardRowDTO> getListDataProvider() {
            return getData();
        }
    }
    
    private class TotalRankColumn extends LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, String> {
        public TotalRankColumn() {
            super(new TextCell(), SortingOrder.ASCENDING, LeaderboardPanel.this);
            setHorizontalAlignment(ALIGN_CENTER);
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            final int totalRank = getLeaderboard().getTotalRank(object.competitor);
            return "" + (totalRank == 0 ? "" : totalRank);
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    final int totalRank1 = getLeaderboard().getTotalRank(o1.competitor);
                    final int totalRank2 = getLeaderboard().getTotalRank(o2.competitor);
                    return totalRank1 == 0 ? totalRank2 == 0 ? 0 : 1 : totalRank2 == 0 ? -1 : totalRank1 - totalRank2;
                }
            };
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.totalRegattaRank()), stringMessages.rankColumnTooltip());
        }
    }

    private class StartingTackColumn extends DetailTypeColumn<Tack, String> {
        public StartingTackColumn(LegDetailField<Tack> field, String headerStyle, String columnStyle) {
            super(DetailType.START_TACK, field, new TextCell(), headerStyle, columnStyle, LeaderboardPanel.this);
        }

        @Override
        public String getValue(LeaderboardRowDTO row) {
            return getField().get(row) == null ? null : getField().get(row) == Tack.PORT ? stringMessages.portTack() : stringMessages.starboardTack();
        }
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, CompetitorSelectionProvider competitorSelectionProvider,
            String leaderboardName, ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent, boolean showRaceDetails) {
        this(sailingService, asyncActionsExecutor, settings, false, /* preSelectedRace */null, competitorSelectionProvider,
                null, leaderboardName, errorReporter, stringMessages, userAgent, showRaceDetails);
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, boolean isEmbedded, RaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent, boolean showRaceDetails) {
        this(sailingService, asyncActionsExecutor, settings, isEmbedded, preSelectedRace, competitorSelectionProvider, new Timer(
                // perform the first request as "live" but don't by default auto-play
                PlayModes.Live, PlayStates.Paused, /* delayBetweenAutoAdvancesInMilliseconds */3000l), leaderboardGroupName,
                leaderboardName, errorReporter, stringMessages, userAgent, showRaceDetails,
                /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* optionalRaceTimesInfoProvider */ null,
                /* autoExpandLastRaceColumn */ false, /* adjustTimerDelay */ true, /*autoApplyTop30Filter*/ false, false);
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, boolean isEmbedded, RaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent, boolean showRaceDetails, CompetitorFilterPanel competitorSearchTextBox,
            boolean showSelectionCheckbox, RaceTimesInfoProvider optionalRaceTimesInfoProvider,
            boolean autoExpandLastRaceColumn, boolean adjustTimerDelay, boolean autoApplyTopNFilter, boolean showCompetitorFilterStatus) {
        this.setTitle(stringMessages.leaderboard());
        this.showSelectionCheckbox = showSelectionCheckbox;
        this.showRaceDetails = showRaceDetails;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.isEmbedded = isEmbedded;
        this.preSelectedRace = preSelectedRace;
        this.competitorSelectionProvider = competitorSelectionProvider;
        competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
        this.setLeaderboardName(leaderboardName);
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.selectedLegDetails = new ArrayList<DetailType>();
        this.selectedRaceDetails = new ArrayList<DetailType>();
        this.selectedOverallDetailColumns = new ArrayList<DetailType>();
        this.raceTimesInfoProvider = optionalRaceTimesInfoProvider;
        this.selectedManeuverDetails = new ArrayList<DetailType>();
        this.adjustTimerDelay = adjustTimerDelay;
        this.initialCompetitorFilterHasBeenApplied = !autoApplyTopNFilter;
        this.showCompetitorFilterStatus = showCompetitorFilterStatus;
        overallDetailColumnMap = createOverallDetailColumnMap();
        settingsUpdatedExplicitly = !settings.isUpdateUponPlayStateChange();
        this.leaderboardUpdateListener = new ArrayList<LeaderboardUpdateListener>();
        if (settings.getLegDetailsToShow() != null) {
            selectedLegDetails.addAll(settings.getLegDetailsToShow());
        }
        if (settings.getManeuverDetailsToShow() != null) {
            selectedManeuverDetails.addAll(settings.getManeuverDetailsToShow());
        }
        if (settings.getRaceDetailsToShow() != null) {
            selectedRaceDetails.addAll(settings.getRaceDetailsToShow());
        }
        if (settings.getOverallDetailsToShow() != null) {
            selectedOverallDetailColumns.addAll(settings.getOverallDetailsToShow());
        }
        setAutoExpandPreSelectedRace(settings.isAutoExpandPreSelectedRace());
        this.autoExpandLastRaceColumn = autoExpandLastRaceColumn;
        if (settings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setRefreshInterval(settings.getDelayBetweenAutoAdvancesInMilliseconds());
        }
        this.timer = timer;
        timer.addPlayStateListener(this);
        timer.addTimeListener(this);
        switch (settings.getActiveRaceColumnSelectionStrategy()) {
        case EXPLICIT:
            if (preSelectedRace == null) {
                raceColumnSelection = new ExplicitRaceColumnSelection();
            } else {
                raceColumnSelection = new ExplicitRaceColumnSelectionWithPreselectedRace(preSelectedRace);
            }
            break;
        case LAST_N:
            setRaceColumnSelectionToLastNStrategy(settings.getNumberOfLastRacesToShow());
            break;
        }
        totalRankColumn = new TotalRankColumn();
        selectionCheckboxColumn = new LeaderboardSelectionCheckboxColumn(competitorSelectionProvider);
        RACE_COLUMN_HEADER_STYLE = tableResources.cellTableStyle().cellTableRaceColumnHeader();
        LEG_COLUMN_HEADER_STYLE = tableResources.cellTableStyle().cellTableLegColumnHeader();
        LEG_DETAIL_COLUMN_HEADER_STYLE = tableResources.cellTableStyle().cellTableLegDetailColumnHeader();
        RACE_COLUMN_STYLE = tableResources.cellTableStyle().cellTableRaceColumn();
        LEG_COLUMN_STYLE = tableResources.cellTableStyle().cellTableLegColumn();
        LEG_DETAIL_COLUMN_STYLE = tableResources.cellTableStyle().cellTableLegDetailColumn();
        TOTAL_COLUMN_STYLE = tableResources.cellTableStyle().cellTableTotalColumn();
        leaderboardTable = new SortedCellTableWithStylableHeaders<LeaderboardRowDTO>(
        /* pageSize */10000, tableResources);
        leaderboardTable.addCellPreviewHandler(new CellPreviewEvent.Handler<LeaderboardRowDTO>() {
            @Override
            public void onCellPreview(CellPreviewEvent<LeaderboardRowDTO> event) {
                if (BrowserEvents.FOCUS.equals(event.getNativeEvent().getType())) {
                    elementToBlur = event.getNativeEvent().getEventTarget().cast();
                    elementToBlur.blur();
                    blurInOnSelectionChanged = 2; // blur a couple of times; doing it one time only doesn't seem to work reliably
                    blurFocusedElementAfterSelectionChange();
                }
            }
        });
        leaderboardTable.ensureDebugId("LeaderboardCellTable");
        getLeaderboardTable().setWidth("100%");
        leaderboardSelectionModel = new MultiSelectionModel<LeaderboardRowDTO>();
        // remember handler registration so we can temporarily remove it and re-add it to suspend selection events while we're actively changing it
        selectionChangeHandler = new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<CompetitorDTO> selection = new ArrayList<CompetitorDTO>();
                for (LeaderboardRowDTO row : getSelectedRows()) {
                    selection.add(row.competitor);
                }
                LeaderboardPanel.this.competitorSelectionProvider.setSelection(selection, /* listenersNotToNotify */LeaderboardPanel.this);
                if (blurInOnSelectionChanged > 0) {
                    blurInOnSelectionChanged--;
                    blurFocusedElementAfterSelectionChange();
                }
            }
        };
        leaderboardAsTableSelectionModelRegistration = leaderboardSelectionModel.addSelectionChangeHandler(selectionChangeHandler);
        leaderboardTable.setSelectionModel(leaderboardSelectionModel, selectionCheckboxColumn.getSelectionManager());
        setShowAddedScores(settings.isShowAddedScores());
        setShowCompetitorSailId(settings.isShowCompetitorSailIdColumn());
        setShowCompetitorFullName(settings.isShowCompetitorFullNameColumn());
        setShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(settings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor());
        if (timer.isInitialized()) {
            loadCompleteLeaderboard(getLeaderboardDisplayDate());
        }

        contentPanel = new VerticalPanel();
        contentPanel.setStyleName(STYLE_LEADERBOARD_CONTENT);
        
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        busyIndicator.ensureDebugId("BusyIndicator");
        // the information panel
        if (!isEmbedded) {
            Widget toolbarPanel = createToolbarPanel();
            contentPanel.add(toolbarPanel);
        }
        if (competitorSearchTextBox != null) {
            contentPanel.add(competitorSearchTextBox);
            contentPanel.add(busyIndicator);
            competitorSearchTextBox.getSettingsButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new SettingsDialog<LeaderboardSettings>(LeaderboardPanel.this, stringMessages).show();
                }
            });
            this.competitorFilterPanel = competitorSearchTextBox;
        }
        SortedCellTable<LeaderboardRowDTO> leaderboardTable = getLeaderboardTable();
        leaderboardTable.getElement().getStyle().setMarginTop(5, Unit.PX);
        filterControlPanel = new HorizontalPanel();
        filterControlPanel.setStyleName("LeaderboardPanel-FilterControl-Panel");
        contentPanel.add(leaderboardTable);
        if (showCompetitorFilterStatus) {
            contentPanel.add(createFilterDeselectionControl());
        }
        setWidget(contentPanel);
        raceNameForDefaultSorting = settings.getNameOfRaceToSort();
    }

    private Widget createToolbarPanel() {
        FlowPanel informationPanel = new FlowPanel();
        informationPanel.setStyleName(STYLE_LEADERBOARD_INFO);
        scoreCorrectionLastUpdateTimeLabel = new Label("");
        scoreCorrectionCommentLabel = new Label("");
        informationPanel.add(scoreCorrectionCommentLabel);
        informationPanel.add(scoreCorrectionLastUpdateTimeLabel);

        liveRaceLabel = new Label(stringMessages.live());
        liveRaceLabel.setStyleName(STYLE_LEADERBOARD_LIVE_RACE);
        liveRaceLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        liveRaceLabel.getElement().getStyle().setColor(IS_LIVE_TEXT_COLOR);
        liveRaceLabel.setVisible(false);
        informationPanel.add(liveRaceLabel);
        
        // the toolbar panel
        DockPanel toolbarPanel = new DockPanel();
        toolbarPanel.ensureDebugId("ToolbarPanel");
        toolbarPanel.setStyleName(STYLE_LEADERBOARD_TOOLBAR);
        if (!isEmbedded) {
            toolbarPanel.add(informationPanel, DockPanel.WEST);
            toolbarPanel.add(busyIndicator, DockPanel.WEST);
        }
        toolbarPanel.setWidth("100%");
        toolbarPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        ClickHandler playPauseHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (LeaderboardPanel.this.timer.getPlayState() == PlayStates.Playing) {
                    LeaderboardPanel.this.timer.pause();
                } else {
                    // playing the standalone leaderboard means putting it into live mode
                    LeaderboardPanel.this.timer.setPlayMode(PlayModes.Live);
                }
            }
        };
        pauseIcon = resources.autoRefreshEnabledIcon();
        playIcon = resources.autoRefreshDisabledIcon();
        refreshAndSettingsPanel = new HorizontalPanel();
        refreshAndSettingsPanel.ensureDebugId("RefreshAndSettingsPanel");
        refreshAndSettingsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        FlowPanel refreshPanel = new FlowPanel();
        refreshPanel.addStyleName("refreshPanel");
        toolbarPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        toolbarPanel.addStyleName("refreshAndSettings");
        playPause = new Anchor(getPlayPauseImgHtml(timer.getPlayState()));
        playPause.ensureDebugId("PlayAndPauseAnchor");
        playPause.addClickHandler(playPauseHandler);
        playStateChanged(timer.getPlayState(), timer.getPlayMode());
        refreshPanel.add(playPause);

        refreshAndSettingsPanel.add(refreshPanel);
        toolbarPanel.add(refreshAndSettingsPanel, DockPanel.EAST);
        return toolbarPanel;
    }

    private Widget createFilterDeselectionControl() {
        filterStatusLabel = new Label();
        filterStatusLabel.setStyleName("LeaderboardPanel-FilterControl-StatusLabel");
        filterStatusLabel.setText("");
        filterControlPanel.add(filterStatusLabel);
        filterClearButton = new Button(stringMessages.showAll());
        filterClearButton.setStyleName("LeaderboardPanel-FilterClear-Button");
        filterClearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getBusyIndicator().setBusy(true);
                competitorFilterPanel.clearAllActiveFilters();
                timeChanged(new Date(), null);
            }
        });
        filterControlPanel.add(filterClearButton);
        filterControlPanel.setCellHorizontalAlignment(filterClearButton, HasHorizontalAlignment.ALIGN_RIGHT);
        setFilterControlStatus();
        return filterControlPanel;
    }
    
    private void setFilterControlStatus() {
        if (showCompetitorFilterStatus) {
            boolean filtersActive = competitorSelectionProvider.hasActiveFilters();
            if (filtersActive) {
                String labelText = "";
                for (Filter<CompetitorDTO> filter : competitorSelectionProvider.getCompetitorsFilterSet().getFilters()) {
                    if (filter instanceof FilterWithUI<?>) {
                        labelText += ((FilterWithUI<CompetitorDTO>)filter).getLocalizedDescription(stringMessages) + ", ";
                    } else {
                        labelText += filter.getName() + ", ";
                    }
                }
                filterStatusLabel.setText("Active Filter(s): " + labelText.substring(0, labelText.length()-2));
                filterClearButton.setVisible(true);
                filterControlPanel.setVisible(true);
            } else {
                filterStatusLabel.setText("");
                filterClearButton.setVisible(false);
                filterControlPanel.setVisible(false);
                if (competitorFilterPanel != null) {
                    competitorFilterPanel.clearSelection();
                }
            }
        }
    }
    
    private static class TotalDistanceTraveledInMetersField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalDistanceTraveledInMeters;
        }
    }
    
    private static class TotalAverageSpeedOverGroundField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            final Double result;
            if (row.totalDistanceTraveledInMeters != null && row.totalTimeSailedInSeconds != null && row.totalTimeSailedInSeconds != 0.0) {
                result = row.totalDistanceTraveledInMeters / row.totalTimeSailedInSeconds / Mile.METERS_PER_NAUTICAL_MILE * 3600;
            } else {
                result = null;
            }
            return result;
        }
    }

    private Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> createOverallDetailColumnMap() {
        Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result = new HashMap<>();
        result.put(DetailType.TOTAL_DISTANCE_TRAVELED,
                new FormattedDoubleDetailTypeColumn(DetailType.TOTAL_DISTANCE_TRAVELED,
                        new TotalDistanceTraveledInMetersField(), RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE, this));
        
        result.put(DetailType.TOTAL_AVERAGE_SPEED_OVER_GROUND,
                new FormattedDoubleDetailTypeColumn(DetailType.TOTAL_AVERAGE_SPEED_OVER_GROUND,
                        new TotalAverageSpeedOverGroundField(), RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE, this));

        result.put(DetailType.MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS, new MaxSpeedOverallColumn(RACE_COLUMN_HEADER_STYLE,
                RACE_COLUMN_STYLE, this));
        
        result.put(DetailType.TOTAL_TIME_SAILED_IN_SECONDS, createOverallTimeTraveledColumn());
        return result;
    }

    private OverallTimeTraveledColumn createOverallTimeTraveledColumn() {
        return new OverallTimeTraveledColumn(this, stringMessages, RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE,
                LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE);
    }

    /**
     * Largely for subclasses to add more stuff to this panel, such as more toolbar buttons.
     */
    protected HorizontalPanel getRefreshAndSettingsPanel() {
        return refreshAndSettingsPanel;
    }

    private RaceColumnDTO getRaceByName(String raceName) {
        if (getLeaderboard() != null) {
            for (RaceColumnDTO race : getLeaderboard().getRaceList()) {
                for (FleetDTO fleet : race.getFleets()) {
                    if (race.getRaceIdentifier(fleet) != null
                            && raceName.equals(race.getRaceIdentifier(fleet).getRaceName())) {
                        return race;
                    }
                }
            }
        }
        return null;
    }

    private RaceColumnDTO getRaceByColumnName(String columnName) {
        if (getLeaderboard() != null) {
            for (RaceColumnDTO race : getLeaderboard().getRaceList()) {
                if (columnName.equals(race.getRaceColumnName())) {
                    return race;
                }
            }
        }
        return null;
    }

    private RaceColumn<?> getRaceColumnByRaceName(String raceName) {
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDTO, ?> column = getLeaderboardTable().getColumn(i);
            if (column instanceof RaceColumn<?>) {
                RaceColumnDTO raceInLeaderboard = ((RaceColumn<?>) column).getRace();
                for (FleetDTO fleet : raceInLeaderboard.getFleets()) {
                    final RegattaAndRaceIdentifier raceIdentifier = raceInLeaderboard.getRaceIdentifier(fleet);
                    if (raceIdentifier != null && raceIdentifier.getRaceName().equals(raceName)) {
                        return (RaceColumn<?>) column;
                    }
                }
            }
        }
        return null;
    }

    private RaceColumn<?> getRaceColumnByRaceColumnName(String raceColumnName) {
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDTO, ?> column = getLeaderboardTable().getColumn(i);
            if (column instanceof RaceColumn<?>) {
                if (((RaceColumn<?>) column).getRaceColumnName().equals(raceColumnName)) {
                    return (RaceColumn<?>) column;
                }
            }
        }
        return null;
    }

    private SafeHtml getPlayPauseImgHtml(PlayStates playState) {
        if (playState == PlayStates.Playing) {
            return AbstractImagePrototype.create(pauseIcon).getSafeHtml();
        } else {
            return AbstractImagePrototype.create(playIcon).getSafeHtml();
        }
    }

    private void setDelayInMilliseconds(long delayInMilliseconds) {
        // If the timer is currently in a mode that causes null to be used as the leaderboard request time stamp,
        // don't let a live play delay change cause another load request by unregistering this panel as timer
        // listener before and re-registering it after adjusting the live play delay.
        if (useNullAsTimePoint()) {
            timer.removeTimeListener(this);
        }
        timer.setLivePlayDelayInMillis(delayInMilliseconds);
        if (useNullAsTimePoint()) {
            timer.addTimeListener(this);
        }
    }

    private boolean isAutoExpandPreSelectedRace() {
        return autoExpandPreSelectedRace;
    }
    
    private boolean isAutoExpandLastRaceColumn() {
        return autoExpandLastRaceColumn;
    }

    private void setAutoExpandPreSelectedRace(boolean autoExpandPreSelectedRace) {
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        if (autoExpandPreSelectedRace) {
            autoExpandPerformedOnce = false;
        }
    }
    
    private boolean isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor() {
        return showOverallColumnWithNumberOfRacesCompletedPerCompetitor;
    }
    
    private void setShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor) {
        this.showOverallColumnWithNumberOfRacesCompletedPerCompetitor = showOverallColumnWithNumberOfRacesCompletedPerCompetitor;
    }
    
    private boolean isShowAddedScores() {
        return showAddedScores;
    }
    
    private void setShowAddedScores(boolean showAddedScores) {
        this.showAddedScores = showAddedScores;
    }
    
    private boolean isShowCompetitorSailId() {
        return showCompetitorSailId;
    }
    
    private void setShowCompetitorSailId(boolean showCompetitorSailId) {
        this.showCompetitorSailId = showCompetitorSailId;
    }
    
    private boolean isShowCompetitorFullName() {
        return showCompetitorFullName;
    }
    
    private void setShowCompetitorFullName(boolean showCompetitorFullName) {
        this.showCompetitorFullName = showCompetitorFullName;
    }
    
    /**
     * The time point for which the leaderboard currently shows results. In {@link PlayModes#Replay replay mode} this is
     * the {@link #timer}'s time point. In {@link PlayModes#Live live mode} the {@link #timer}'s time is quantized to
     * the closest full second to increase the likelihood of cache hits in the back end.
     */
    protected Date getLeaderboardDisplayDate() {
        return timer.getTime();
    }

    /**
     * adds the <code>column</code> to the right end of the {@link #getLeaderboardTable() leaderboard table} and sets
     * the column style according to the {@link LeaderboardSortableColumnWithMinMax#getColumnStyle() column's style definition}.
     */
    protected void addColumn(AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> column) {
        leaderboardTable.addColumn(column, column.getHeader(), column.getComparator(), column
                .getPreferredSortingOrder().isAscending());
        String columnStyle = column.getColumnStyle();
        if (columnStyle != null) {
            getLeaderboardTable().addColumnStyleName(getLeaderboardTable().getColumnCount() - 1, columnStyle);
        }
    }

    protected void insertColumn(int beforeIndex, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> column) {
        // remove column styles of those columns whose index will shift right by
        // one:
        removeColumnStyles(beforeIndex);
        getLeaderboardTable().insertColumn(beforeIndex, column, column.getHeader(), column.getComparator(),
                column.getPreferredSortingOrder().isAscending());
        addColumnStyles(beforeIndex);
    }

    private void addColumnStyles(int startColumn) {
        for (int i = startColumn; i < getLeaderboardTable().getColumnCount(); i++) {
            AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> columnToRemoveStyleFor = (AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>) getLeaderboardTable()
                    .getColumn(i);
            String columnStyle = columnToRemoveStyleFor.getColumnStyle();
            if (columnStyle != null) {
                getLeaderboardTable().addColumnStyleName(i, columnStyle);
            }
        }
    }

    private void removeColumnStyles(int startColumn) {
        for (int i = startColumn; i < getLeaderboardTable().getColumnCount(); i++) {
            AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> columnToRemoveStyleFor = (AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>) getLeaderboardTable()
                    .getColumn(i);
            String columnStyle = columnToRemoveStyleFor.getColumnStyle();
            if (columnStyle != null) {
                getLeaderboardTable().removeColumnStyleName(i, columnStyle);
            }
        }
    }

    /**
     * removes the column specified by <code>columnIndex</code> from the {@link #getLeaderboardTable() leaderboard
     * table} and fixes the column styles again (see {@link #addColumnStyles(int)}).
     */
    protected void removeColumn(int columnIndex) {
        Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(columnIndex);
        if (c instanceof ExpandableSortableColumn<?>) {
            ExpandableSortableColumn<?> expandableColumn = (ExpandableSortableColumn<?>) c;
            if (expandableColumn.isExpanded()) {
                // remove expanded child columns from the leaderboard...
                expandableColumn.toggleExpansion();
                // them remember that column c was expanded:
                expandableColumn.setExpanded(true);
            }
        }
        removeColumnStyles(/* startColumn */columnIndex);
        getLeaderboardTable().removeColumn(columnIndex);
        addColumnStyles(/* startColumn */columnIndex);
    }

    protected void removeColumn(Column<LeaderboardRowDTO, ?> c) {
        int columnIndex = getLeaderboardTable().getColumnIndex(c);
        if (columnIndex != -1) {
            removeColumn(columnIndex);
        }
    }

    private void loadCompleteLeaderboard(Date date) {
        if (needsDataLoading()) {
            GetLeaderboardByNameAction getLeaderboardByNameAction = new GetLeaderboardByNameAction(sailingService,
                    getLeaderboardName(), useNullAsTimePoint() ? null : date,
                    /* namesOfRacesForWhichToLoadLegDetails */getNamesOfExpandedRaces(), shallAddOverallDetails(), /* previousLeaderboard */
                    getLeaderboard(), isFillNetPointsUncorrected(), timer, errorReporter, stringMessages);
            this.asyncActionsExecutor.execute(getLeaderboardByNameAction, LOAD_LEADERBOARD_DATA_CATEGORY,
                    new AsyncCallback<LeaderboardDTO>() {
                        @Override
                        public void onSuccess(LeaderboardDTO result) {
                            updateLeaderboard(result);
                            getBusyIndicator().setBusy(false);
                        }
        
                        @Override
                        public void onFailure(Throwable caught) {
                            getBusyIndicator().setBusy(false);
                            getErrorReporter()
                                    .reportError("Error trying to obtain leaderboard contents: " + caught.getMessage(),
                                            true /* silentMode */);
                        }
                    });
        } else {
            getBusyIndicator().setBusy(false);
        }
    }

    protected boolean isFillNetPointsUncorrected() {
        return false;
    }

    /**
     * In {@link PlayModes#Live live mode}, when {@link #loadCompleteLeaderboard(Date) loading the leaderboard contents}, <code>null</code>
     * is used as time point. The condition for this is encapsulated in this method so others can find out. For example, when a time change
     * is signaled due to local offset / delay adjustments, no additional call to {@link #loadCompleteLeaderboard(Date)} would be required
     * as <code>null</code> will be passed in any case, not being affected by local time offsets.
     */
    private boolean useNullAsTimePoint() {
        return timer.getPlayMode() == PlayModes.Live;
    }

    private boolean needsDataLoading() {
        return isVisible();
    }

    /**
     * Determine from column expansion state which races need their leg details
     */
    private Collection<String> getNamesOfExpandedRaces() {
        Collection<String> namesOfExpandedRaces = new ArrayList<String>();
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDTO, ?> column = getLeaderboardTable().getColumn(i);
            if (column instanceof RaceColumn<?>) {
                RaceColumn<?> raceColumn = (RaceColumn<?>) column;
                if (raceColumn.isExpanded()) {
                    namesOfExpandedRaces.add(raceColumn.getRaceColumnName());
                }
            }
        }
        return namesOfExpandedRaces;
    }

    private boolean shallAddOverallDetails() {
        return !selectedOverallDetailColumns.isEmpty();
    }
    
    private void applyTop30FilterIfCompetitorSizeGreaterEqual40(LeaderboardDTO leaderboard) {
        int maxRaceRank = 30;
        if (leaderboard.competitors.size() >= 40) {
            CompetitorRaceRankFilter raceRankFilter = new CompetitorRaceRankFilter();
            raceRankFilter.setLeaderboardFetcher(this);
            raceRankFilter.setSelectedRace(preSelectedRace);
            raceRankFilter.setQuickRankProvider(this.competitorFilterPanel.getQuickRankProvider());
            raceRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
            raceRankFilter.setValue(maxRaceRank);
            FilterSet<CompetitorDTO, Filter<CompetitorDTO>> activeFilterSet = 
                    competitorSelectionProvider.getOrCreateCompetitorsFilterSet(stringMessages.topNCompetitorsByRaceRank(maxRaceRank));
            activeFilterSet.addFilter(raceRankFilter);
            competitorSelectionProvider.setCompetitorsFilterSet(activeFilterSet);
        }
    }

    /**
     * Assigns <code>leaderboard</code> to {@link #leaderboard} and updates the UI accordingly. Also updates the min/max
     * values on the columns.
     */
    protected void updateLeaderboard(LeaderboardDTO leaderboard) {
        if (leaderboard != null) {
            Collection<RaceColumn<?>> columnsToCollapseAndExpandAgain = getExpandedRaceColumnsWhoseDisplayedLegCountChanged(leaderboard);
            for (RaceColumn<?> columnToCollapseAndExpandAgain : columnsToCollapseAndExpandAgain) {
                columnToCollapseAndExpandAgain.toggleExpansion();
            }
            competitorSelectionProvider.setCompetitors(leaderboard.competitors, /* listenersNotToNotify */this);
            if (!initialCompetitorFilterHasBeenApplied) {
                applyTop30FilterIfCompetitorSizeGreaterEqual40(leaderboard);
                initialCompetitorFilterHasBeenApplied = true;
            }
            raceColumnSelection.autoUpdateRaceColumnSelectionForUpdatedLeaderboard(getLeaderboard(), leaderboard);
            setLeaderboard(leaderboard);
            adjustColumnLayout(leaderboard);
            updateRaceColumnDTOsToRaceColumns(leaderboard);
            for (RaceColumn<?> columnToCollapseAndExpandAgain : columnsToCollapseAndExpandAgain) {
                columnToCollapseAndExpandAgain.toggleExpansion();
            }
            adjustDelayToLive();
            final Map<CompetitorDTO, LeaderboardRowDTO> rowsToDisplay = getRowsToDisplay();
            Set<LeaderboardRowDTO> rowsToAdd = new HashSet<>(rowsToDisplay.values());
            Map<Integer, LeaderboardRowDTO> rowsToUpdate = new HashMap<>();
            synchronized (getData().getList()) {
                int index = 0;
                for (Iterator<LeaderboardRowDTO> i = getData().getList().iterator(); i.hasNext(); ) {
                    LeaderboardRowDTO oldRow = i.next();
                    LeaderboardRowDTO newRow = rowsToDisplay.get(oldRow.competitor);
                    if (newRow != null) {
                        rowsToUpdate.put(index++, newRow); // update row in place, preserving its selection state
                        rowsToAdd.remove(newRow); // no need to add this row when it was updated in-place
                    } else {
                        i.remove(); // old row's competitor not found in new rows' competitors; remove old row from table
                    }
                }
                for (Entry<Integer, LeaderboardRowDTO> updateEntry : rowsToUpdate.entrySet()) {
                    LeaderboardRowDTO oldElement = getData().getList().set(updateEntry.getKey(), updateEntry.getValue());
                    leaderboardSelectionModel.setSelected(oldElement, false); // make sure the old element is no longer part of the selection
                    updateSelection(updateEntry.getValue());
                }
                for (LeaderboardRowDTO rowToAdd : rowsToAdd) {
                    getData().getList().add(rowToAdd);
                    updateSelection(rowToAdd);
                }
            }
            RaceColumn<?> lastRaceColumn = null;
            for (int i=getLeaderboardTable().getColumnCount()-1; i>=0; i--) {
                if (getLeaderboardTable().getColumn(i) instanceof RaceColumn<?>) {
                    lastRaceColumn = (RaceColumn<?>) getLeaderboardTable().getColumn(i);
                    break;
                }
            }
            for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
                AbstractSortableColumnWithMinMax<?, ?> c = (AbstractSortableColumnWithMinMax<?, ?>) getLeaderboardTable().getColumn(i);
                c.updateMinMax();
                // Toggle pre-selected race, if the setting is set and it isn't open yet, or the last race column if that was requested
                if ((!autoExpandPerformedOnce && isAutoExpandPreSelectedRace() && c instanceof RaceColumn<?>
                        && ((RaceColumn<?>) c).getRace().hasTrackedRace(preSelectedRace)) ||
                        (isAutoExpandLastRaceColumn() && c == lastRaceColumn)) {
                    ExpandableSortableColumn<?> expandableSortableColumn = (ExpandableSortableColumn<?>) c;
                    if (!expandableSortableColumn.isExpanded()) {
                        expandableSortableColumn.toggleExpansion();
                        autoExpandPerformedOnce = true;
                    }
                }
                if (c instanceof RaceColumn && ((RaceColumn<?>) c).getRace().hasTrackedRace(preSelectedRace)) {
                    RaceColumnDTO raceColumn = ((RaceColumn<?>) c).getRace();
                    informLeaderboardUpdateListenersAboutRaceSelected(preSelectedRace, raceColumn);
                }
            }
            if (leaderboardTable.getCurrentlySortedColumn() != null) {
                leaderboardTable.sort();
            } else {
                AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> columnToSortFor = getDefaultSortColumn();
                leaderboardTable.sortColumn(columnToSortFor, columnToSortFor.getPreferredSortingOrder().isAscending());
            }
            
            if (!isEmbedded) {
                scoreCorrectionCommentLabel.setText(leaderboard.getComment() != null ? leaderboard.getComment() : "");
                if (leaderboard.getTimePointOfLastCorrectionsValidity() != null) {
                    Date lastCorrectionDate = leaderboard.getTimePointOfLastCorrectionsValidity();
                    String lastUpdate = DateAndTimeFormatterUtil.longDateFormatter.render(lastCorrectionDate) + ", "
                            + DateAndTimeFormatterUtil.longTimeFormatter.render(lastCorrectionDate);
                    scoreCorrectionLastUpdateTimeLabel.setText(stringMessages.lastScoreUpdate() + ": " + lastUpdate);
                } else {
                    scoreCorrectionLastUpdateTimeLabel.setText("");
                }
                
                List<com.sap.sse.common.Util.Pair<RaceColumnDTO, FleetDTO>> liveRaces = leaderboard.getLiveRaces(timer.getLiveTimePointInMillis());
                boolean hasLiveRace = !liveRaces.isEmpty();
                if (hasLiveRace) {
                    String liveRaceText = "";
                    if(liveRaces.size() == 1) {
                        com.sap.sse.common.Util.Pair<RaceColumnDTO, FleetDTO> liveRace = liveRaces.get(0);
                        liveRaceText = stringMessages.raceIsLive("'" + liveRace.getA().getRaceColumnName() + "'");
                    } else {
                        String raceNames = "";
                        for (com.sap.sse.common.Util.Pair<RaceColumnDTO, FleetDTO> liveRace : liveRaces) {
                            raceNames += "'" + liveRace.getA().getRaceColumnName() + "', ";
                        }
                        // remove last ", "
                        raceNames = raceNames.substring(0, raceNames.length() - 2);
                        liveRaceText = stringMessages.racesAreLive(raceNames);
                    }
                    liveRaceLabel.setText(liveRaceText);
                } else {
                    liveRaceLabel.setText("");
                }
                scoreCorrectionLastUpdateTimeLabel.setVisible(!hasLiveRace);
                liveRaceLabel.setVisible(hasLiveRace);
            }
            informLeaderboardUpdateListenersAboutLeaderboardUpdated(leaderboard);
            getBusyIndicator().setBusy(false);
        }
    }

    /**
     * Adjusts the row's selection in the {@link #leaderboardSelectionModel} so it matches its selection state in
     * the {@link #competitorSelectionProvider}.
     */
    private void updateSelection(LeaderboardRowDTO row) {
        final boolean shallBeSelected = competitorSelectionProvider.isSelected(row.competitor);
        if (leaderboardAsTableSelectionModelRegistration != null) {
            // suspend selection events while actively adjusting the leaderboardSelectionModel to match the competitorSelectionProvider
            leaderboardAsTableSelectionModelRegistration.removeHandler();
            leaderboardAsTableSelectionModelRegistration = null;
        }
        if (leaderboardSelectionModel.isSelected(row) != shallBeSelected) {
            leaderboardSelectionModel.setSelected(row, shallBeSelected);
        }
        // register the selection change handler again
        leaderboardAsTableSelectionModelRegistration = leaderboardTable.getSelectionModel().addSelectionChangeHandler(selectionChangeHandler);
    }

    /**
     * The race columns hold a now outdated copy of a {@link RaceColumnDTO} which needs to be updated from the {@link LeaderboardDTO} just received
     */
    private void updateRaceColumnDTOsToRaceColumns(LeaderboardDTO leaderboard) {
    	for (RaceColumnDTO newRace : leaderboard.getRaceList()) {
    		RaceColumn<?> raceColumn = getRaceColumnByRaceColumnName(newRace.getName());
    		if (raceColumn != null) {
    			raceColumn.setRace(newRace);
    		}
    	}
    }

	/**
     * Due to a course change, a race may change its number of legs. All expanded race columns that show leg columns and
     * whose leg count changed need to be collapsed before the leaderboard is replaced, and expanded afterwards again.
     * Race columns whose toggling is {@link ExpandableSortableColumn#isTogglingInProcess() currently in progress} are
     * not considered because their new state will be considered after replacing anyhow.
     * 
     * @param newLeaderboard
     *            the new leaderboard before assigning to {@link #leaderboard}
     * @return the columns that were collapsed in this step and that shall be expanded again after the leaderboard has
     *         been replaced
     */
    private Collection<RaceColumn<?>> getExpandedRaceColumnsWhoseDisplayedLegCountChanged(LeaderboardDTO newLeaderboard) {
        Set<RaceColumn<?>> result = new HashSet<RaceColumn<?>>();
        if (selectedRaceDetails.contains(DetailType.DISPLAY_LEGS)) {
            for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
                Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(i);
                if (c instanceof RaceColumn<?>) {
                    RaceColumn<?> rc = (RaceColumn<?>) c;
                    // If the new leaderboard no longer contains the column, getLegCount will return -1, causing the column
                    // to be collapsed if it was expanded. This is correct because otherwise, removing it would no longer
                    // know the correct leg count.
                    if (!rc.isTogglingInProcess() && rc.isExpanded()) {
                        int oldLegCount = getLeaderboard().getLegCount(rc.getRaceColumnName(), preSelectedRace);
                        int newLegCount = newLeaderboard.getLegCount(rc.getRaceColumnName(), preSelectedRace);
                        if (oldLegCount != newLegCount) {
                            result.add(rc);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Based on the {@link LeaderboardDTO#getDelayToLiveInMillisForLatestRace()} for the race that has the latest
     * {@link RaceColumnDTO#getStartDate(FleetDTO) start time}, automatically adjusts the delay accordingly unless the
     * {@link #adjustTimerDelay} flag is <code>false</code>
     */
    private void adjustDelayToLive() {
        if (adjustTimerDelay) {
            if (leaderboard.getDelayToLiveInMillisForLatestRace() != null) {
                setDelayInMilliseconds(leaderboard.getDelayToLiveInMillisForLatestRace());
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
        if (preSelectedRace == null) {
            for (CompetitorDTO competitor : leaderboard.rows.keySet()) {
                if (Util.contains(allFilteredCompetitors, competitor)) {
                    result.put(competitor, leaderboard.rows.get(competitor));
                }
            }
        } else {
            for (CompetitorDTO competitorInPreSelectedRace : getCompetitors(preSelectedRace)) {
                if (Util.contains(allFilteredCompetitors, competitorInPreSelectedRace)) {
                    result.put(competitorInPreSelectedRace, leaderboard.rows.get(competitorInPreSelectedRace));
                }
            }
        }
        return result;
    }

    /**
     * The {@link LeaderboardDTO} holds {@link LeaderboardDTO#getRaceList() races} as {@link RaceColumnDTO} objects.
     * Those map their fleet names to the {@link RegattaAndRaceIdentifier} which identifies the tracked race
     * representing the fleet race within the race column.
     * <p>
     * 
     * On the other hand, a {@link LeaderboardRowDTO} has {@link LeaderboardEntryDTO}, keyed by race column name. The
     * entry DTOs, in turn, store the {@link RaceIdentifier} of the race in which the respective competitor achieved the
     * score.
     * <p>
     * 
     * With this information it is possible to identify the competitors who participated in a particular tracked race,
     * as identified through the <code>race</code> parameter.
     * 
     * @return all competitors for which the {@link #getLeaderboard() leaderboard} has an entry whose
     *         {@link LeaderboardEntryDTO#race} equals <code>race</code>
     */
    private Iterable<CompetitorDTO> getCompetitors(RaceIdentifier race) {
        Set<CompetitorDTO> result = new HashSet<CompetitorDTO>();
        for (RaceColumnDTO raceColumn : getLeaderboard().getRaceList()) {
            if (raceColumn.hasTrackedRace(race)) {
                for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> e : getLeaderboard().rows.entrySet()) {
                    LeaderboardEntryDTO entry = e.getValue().fieldsByRaceColumnName.get(raceColumn.getRaceColumnName());
                    if (entry != null && entry.race != null && entry.race.equals(race)) {
                        result.add(e.getKey());
                    }
                }
            }
        }
        return result;
    }

    private AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> getDefaultSortColumn() {
        AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> defaultSortColumn = null;
        if (raceNameForDefaultSorting != null) {
            defaultSortColumn = getRaceColumnByRaceName(raceNameForDefaultSorting);
        }
        if (defaultSortColumn == null) {
            defaultSortColumn = getRankColumn();
        }
        return defaultSortColumn;
    }

    private TotalRankColumn getRankColumn() {
        return totalRankColumn;
    }

    protected void setLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public LeaderboardDTO getLeaderboard() {
        return leaderboard;
    }

    private void adjustColumnLayout(LeaderboardDTO leaderboard) {
        int columnIndex = 0;
        columnIndex = ensureSelectionCheckboxColumn(columnIndex);
        columnIndex = ensureRankColumn(columnIndex);
        columnIndex = ensureSailIDAndCompetitorColumn(columnIndex);
        columnIndex = updateCarryColumn(leaderboard, columnIndex);
        adjustOverallDetailColumns(leaderboard, columnIndex);
        // first remove race columns no longer needed:
        removeUnusedRaceColumns(leaderboard);
        if (leaderboard != null) {
            createMissingAndAdjustExistingRaceColumns(leaderboard);
            ensureTotalsColumn();
            updateTotalRacesSailedColumn();
        }
    }

    /**
     * Ensures that the columns requested by {@link #selectedOverallDetailColumns} are in the table. Assumes that if there are
     * any existing overall details columns, they start at <code>indexOfFirstOverallDetailsColumn</code> and are in the order
     * defined by {@link #getAvailableOverallDetailColumnTypes()}.
     * 
     * @param indexOfFirstOverallDetailsColumn
     *            tells the column index for the first overall details column
     */
    private void adjustOverallDetailColumns(LeaderboardDTO leaderboard, int indexOfFirstOverallDetailsColumn) {
        List<AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> overallDetailColumnsToShow = new ArrayList<AbstractSortableColumnWithMinMax<LeaderboardRowDTO,?>>();
        // ensure the ordering in overallDetailColumnsToShow conforms to the ordering of getAvailableOverallDetailColumnTypes()
        for (DetailType overallDetailType : getAvailableOverallDetailColumnTypes()) {
            if (selectedOverallDetailColumns.contains(overallDetailType) && overallDetailColumnMap.containsKey(overallDetailType)) {
                overallDetailColumnsToShow.add(overallDetailColumnMap.get(overallDetailType));
            }
        }
        int currentColumnIndex = indexOfFirstOverallDetailsColumn;
        int i = 0; // index into overallDetailColumnToShow
        Column<LeaderboardRowDTO, ?> currentColumn = currentColumnIndex < getLeaderboardTable().getColumnCount() ?
                getLeaderboardTable().getColumn(currentColumnIndex) : null;
        // repeat until no more column to check for removal and no more column left to check for need to insert
        while (i<overallDetailColumnsToShow.size() || overallDetailColumnMap.values().contains(currentColumn)) {
            if (i<overallDetailColumnsToShow.size() && currentColumn == overallDetailColumnsToShow.get(i)) {
                // found selected column in table; all good, advance both "pointers"
                i++;
                currentColumnIndex++;
                currentColumn = getLeaderboardTable().getColumn(currentColumnIndex);
            } else if (i<overallDetailColumnsToShow.size()) {
                // selected column is missing; insert
                insertColumn(currentColumnIndex++, overallDetailColumnsToShow.get(i++));
            } else {
                // based on the while's condition, currentColumn is an overallDetailsColumnMap value;
                // based on the previous if's failed condition, it is not selected. Remove:
                removeColumn(currentColumnIndex);
                currentColumn = getLeaderboardTable().getColumn(currentColumnIndex);
            }
        }
    }

    /**
     * If header information doesn't match the race column's actual state (tracked races attached meaning expandable;
     * medal race), the column is removed and inserted again
     * 
     * @param raceColumn
     *            the raceColumn to correct.
     * @param selectedRaceColumn
     *            the new race column data for <code>raceColumn</code>
     */
    private void correctColumnData(RaceColumn<?> raceColumn, RaceColumnDTO race) {
        int columnIndex = getRaceColumnPosition(raceColumn);
        if (raceColumn.isExpansionEnabled() != race.hasTrackedRaces() || race.isMedalRace() != raceColumn.isMedalRace()) {
            if (raceColumn.isExpanded()) {
                raceColumn.toggleExpansion(); // remove children from table
            }
            removeColumn(columnIndex);
            insertColumn(columnIndex, createRaceColumn(race));
        }
    }

    /**
     * Removes all RaceColumns, starting at count {@link raceColumnStartIndex raceColumnStartIndex}
     * 
     * @param raceColumnStartIndex
     *            The index of the race column should be deleted from.
     * @param raceName
     *            The name of the racing column until the table should be cleared.
     */
    private void removeRaceColumnFromRaceColumnStartIndexBeforeRace(int raceColumnStartIndex, RaceColumnDTO race) {
        int counter = 0;
        for (int leaderboardposition = 0; leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                RaceColumn<?> raceColumn = (RaceColumn<?>) c;
                if (!raceColumn.getRaceColumnName().equals(race.getRaceColumnName())) {
                    if (raceColumnStartIndex == counter) {
                        removeColumn(raceColumn);
                    }
                } else {
                    return;
                }
                counter++;
            }
        }
    }

    /**
     * Gets a ColumnPosition of a raceColumn
     * 
     * @param raceColumn
     *            The column for which the position is to be found in the leaderboard table
     * @return the position. Returns -1 if raceColumn not existing in leaderboardTable.
     */
    private int getRaceColumnPosition(RaceColumn<?> raceColumn) {
        for (int leaderboardposition = 0; leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                RaceColumn<?> rc = (RaceColumn<?>) c;
                if (rc.equals(raceColumn)) {
                    return leaderboardposition;
                }
            }
        }
        return -1;
    }

    /**
     * This method returns the position where a race column should get inserted.
     * 
     * @param raceName
     *            the name of the race to insert
     * @param listpos
     *            the position of the race in the {@link selectedRaceColumns selectedRaceColumns}
     * @return the position of a race column right before which to insert the race column so that it is the
     *         <code>listpos</code>th race in the table or -1 if no such race column was found, e.g., in case the column
     *         needs to be inserted as the last race in the table
     */
    private int getColumnPositionToInsert(RaceColumnDTO race, int listpos) {
        int raceColumnCounter = 0;
        int noRaceColumnCounter = 0;
        boolean raceColumnFound = false;
        for (int leaderboardPosition = 0; !raceColumnFound
                && leaderboardPosition < getLeaderboardTable().getColumnCount(); leaderboardPosition++) {
            Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(leaderboardPosition);
            if (c instanceof RaceColumn) {
                // RaceColumn<?> raceColumn = (RaceColumn<?>) c;
                if (raceColumnCounter == listpos) {
                    raceColumnFound = true;
                } else {
                    raceColumnCounter++;
                }
            } else {
                noRaceColumnCounter++;
            }
        }
        if (raceColumnFound) {
            return raceColumnCounter + noRaceColumnCounter;
        } else {
            return -1;
        }
    }

    /**
     * Removes all columns of type {@link RaceColumn} from the leaderboard table if their name is not in the list of
     * names of the <code>selectedRaceColumns</code>.
     */
    private void removeRaceColumnsNotSelected(Iterable<RaceColumnDTO> selectedRaceColumns) {
        Set<String> selectedRaceColumnNames = new HashSet<String>();
        for (RaceColumnDTO selectedRaceColumn : selectedRaceColumns) {
            selectedRaceColumnNames.add(selectedRaceColumn.getRaceColumnName());
        }
        List<Column<LeaderboardRowDTO, ?>> columnsToRemove = new ArrayList<Column<LeaderboardRowDTO, ?>>();
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(i);
            if (c instanceof RaceColumn
                    && (leaderboard == null || !selectedRaceColumnNames.contains(((RaceColumn<?>) c)
                            .getRaceColumnName()))) {
                columnsToRemove.add(c);
            }
        }
        for (Column<LeaderboardRowDTO, ?> c : columnsToRemove) {
            removeColumn(c);
        }
    }

    /**
     * Existing and matching race columns may still need to be removed, re-created and inserted because the "tracked"
     * property may have changed, changing the columns expandability.
     */
    private void createMissingAndAdjustExistingRaceColumns(LeaderboardDTO leaderboard) {
        // Correct order of races in selectedRaceColum
        Iterable<RaceColumnDTO> correctedOrderSelectedRaces = raceColumnSelection.getSelectedRaceColumnsOrderedAsInLeaderboard(leaderboard);
        removeRaceColumnsNotSelected(correctedOrderSelectedRaces);
        for (int selectedRaceCount = 0; selectedRaceCount < Util.size(correctedOrderSelectedRaces); selectedRaceCount++) {
            RaceColumnDTO selectedRaceColumn = Util.get(correctedOrderSelectedRaces, selectedRaceCount);
            final RaceColumn<?> raceColumn = selectedRaceColumn == null ? null
                    : getRaceColumnByRaceColumnName(selectedRaceColumn.getName());
            if (raceColumn != null) {
                // remove all raceColumns, starting at a specific selectedRaceCount, up to but excluding the selected
                // raceName with the result that selectedRace is at position selectedRaceCount afterwards
                removeRaceColumnFromRaceColumnStartIndexBeforeRace(selectedRaceCount, selectedRaceColumn);
                correctColumnData(raceColumn, selectedRaceColumn);
            } else {
                // get correct position to insert the column
                int positionToInsert = getColumnPositionToInsert(selectedRaceColumn, selectedRaceCount);
                if (positionToInsert != -1) {
                    insertColumn(positionToInsert, createRaceColumn(selectedRaceColumn));
                } else {
                    // Add the raceColumn with addRaceColumn, if no RaceColumn is existing in leaderboard
                    addRaceColumn(createRaceColumn(selectedRaceColumn));
                }
            }
        }
    }

    protected RaceColumn<?> createRaceColumn(RaceColumnDTO raceInLeaderboard) {
        TextRaceColumn textRaceColumn = new TextRaceColumn(raceInLeaderboard, shallExpandRaceColumn(raceInLeaderboard),
                SortingOrder.ASCENDING, RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE);
        return textRaceColumn;
    }

    /**
     * To be expandable, a race column needs to have one or more tracked races associated and needs to have at least
     * some GPS data available. Wind data is not required for expandability because several metrics can reasonably be
     * determined even without reliable wind information.
     */
    private boolean shallExpandRaceColumn(RaceColumnDTO raceColumnDTO) {
        return showRaceDetails && raceColumnDTO.hasTrackedRaces() && raceColumnDTO.hasGPSData();
    }

    private void removeUnusedRaceColumns(LeaderboardDTO leaderboard) {
        List<Column<LeaderboardRowDTO, ?>> columnsToRemove = new ArrayList<Column<LeaderboardRowDTO, ?>>();
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDTO, ?> c = getLeaderboardTable().getColumn(i);
            if (c instanceof RaceColumn
                    && (leaderboard == null || !leaderboard.raceListContains(((RaceColumn<?>) c).getRaceColumnName()))) {
                columnsToRemove.add(c);
            }
        }
        // Tricky issue: if the race column is currently expanded, we can't know anymore how many detail columns
        // there are because the updated LeaderboardDTO object doesn't contain the race anymore. We have to
        // collapse and remove all LegColumns following the RaceColumn
        for (Column<LeaderboardRowDTO, ?> c : columnsToRemove) {
            removeColumn(c);
        }
    }

    /**
     * If the last column is the totals column, remove it. Add the race column as the last column.
     */
    private void addRaceColumn(RaceColumn<?> raceColumn) {
        if (getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount() - 1) instanceof TotalsColumn) {
            removeColumn(getLeaderboardTable().getColumnCount() - 1);
        }
        addColumn(raceColumn);
    }

    /**
     * The regatta rank column shall be displayed if an only if the {@link DetailType#REGATTA_RANK} detail is selected in the
     * {@link #selectedOverallDetailColumns}. It will then be displayed after the selection checkbox column (if any) and
     * before the sail number / competitor name columns.
     */
    private boolean isShowRegattaRankColumn() {
        return selectedOverallDetailColumns.contains(DetailType.REGATTA_RANK);
    }
    
    /**
     * @param rankColumnIndex
     *            the column index (0-based) where to put the rank column, if needed
     * @return the column index (0-based) for the next column; will equal <code>rankColumnIndex</code> if the rank
     *         column is not {@link #isShowRegattaRankColumn() supposed to be shown}, or one greater otherwise.
     */
    private int ensureRankColumn(int rankColumnIndex) {
        final int indexOfNextColumn = rankColumnIndex + (isShowRegattaRankColumn() ? 1 : 0);
        if (getLeaderboardTable().getColumnCount() > rankColumnIndex) {
            if (isShowRegattaRankColumn()) {
                if (getLeaderboardTable().getColumn(rankColumnIndex) != getRankColumn()) {
                    insertColumn(rankColumnIndex, getRankColumn());
                } // else, the column is needed and is already in place
            } else {
                if (getLeaderboardTable().getColumn(rankColumnIndex) == getRankColumn()) {
                    removeColumn(rankColumnIndex);
                }
            }
        } else {
            if (isShowRegattaRankColumn()) {
                insertColumn(rankColumnIndex, getRankColumn());
            }
        }
        return indexOfNextColumn;
    }

    /**
     * @param selectionCheckboxColumnIndex
     *            the column index (0-based) where to put the selection checkbox column, if needed
     * @return the column index (0-based) for the next column; will equal <code>selectionCheckboxColumnIndex</code> if
     *         the selection checkbox column is not {@link #showSelectionCheckbox supposed to be shown}, or one greater
     *         otherwise.
     */
    private int ensureSelectionCheckboxColumn(int selectionCheckboxColumnIndex) {
        final int indexOfNextColumn = selectionCheckboxColumnIndex + (showSelectionCheckbox ? 1 : 0);
        if (getLeaderboardTable().getColumnCount() > selectionCheckboxColumnIndex) {
            if (showSelectionCheckbox) {
                if (getLeaderboardTable().getColumn(selectionCheckboxColumnIndex) != selectionCheckboxColumn) {
                    insertColumn(selectionCheckboxColumnIndex, selectionCheckboxColumn);
                } // else, the column is needed and is already in place
            } else {
                if (getLeaderboardTable().getColumn(selectionCheckboxColumnIndex) == selectionCheckboxColumn) {
                    removeColumn(selectionCheckboxColumnIndex);
                }
            }
        } else {
            if (showSelectionCheckbox) {
                insertColumn(selectionCheckboxColumnIndex, selectionCheckboxColumn);
            }
        }
        return indexOfNextColumn;
    }

    /**
     * @return the 0-based index for the next column
     */
    private int ensureSailIDAndCompetitorColumn(int columnIndexWhereToInsertTheNextColumn) {
        if (isShowCompetitorSailId()) {
            if (getLeaderboardTable().getColumnCount() <= columnIndexWhereToInsertTheNextColumn
                    || !(getLeaderboardTable().getColumn(columnIndexWhereToInsertTheNextColumn) instanceof SailIDColumn<?>)) {
                insertColumn(columnIndexWhereToInsertTheNextColumn, new SailIDColumn<LeaderboardRowDTO>(new CompetitorFetcher<LeaderboardRowDTO>() {
                    @Override
                    public CompetitorDTO getCompetitor(LeaderboardRowDTO t) {
                        return t.competitor;
                    }
                }));
            }
            columnIndexWhereToInsertTheNextColumn++;
        } else {
            if (getLeaderboardTable().getColumnCount() > columnIndexWhereToInsertTheNextColumn
                    && getLeaderboardTable().getColumn(columnIndexWhereToInsertTheNextColumn) instanceof SailIDColumn<?>) {
                removeColumn(columnIndexWhereToInsertTheNextColumn);
            }
        }
        if (isShowCompetitorFullName()) {
            if (getLeaderboardTable().getColumnCount() <= columnIndexWhereToInsertTheNextColumn
                    || !(getLeaderboardTable().getColumn(columnIndexWhereToInsertTheNextColumn) instanceof CompetitorColumn)) {
                insertColumn(columnIndexWhereToInsertTheNextColumn, createCompetitorColumn());
            }
            columnIndexWhereToInsertTheNextColumn++;
        } else {
            if (getLeaderboardTable().getColumnCount() > columnIndexWhereToInsertTheNextColumn
                    && getLeaderboardTable().getColumn(columnIndexWhereToInsertTheNextColumn) instanceof CompetitorColumn) {
                removeColumn(columnIndexWhereToInsertTheNextColumn);
            }
        }
        return columnIndexWhereToInsertTheNextColumn;
    }

    protected CompetitorColumn createCompetitorColumn() {
        return new CompetitorColumn(new CompetitorColumnBase<LeaderboardRowDTO>(this, getStringMessages(),
                new CompetitorFetcher<LeaderboardRowDTO>() {
            @Override
            public CompetitorDTO getCompetitor(LeaderboardRowDTO t) {
                return t.competitor;
            }
        }));
    }

    private void ensureTotalsColumn() {
        // add a totals column on the right
        if (getLeaderboardTable().getColumnCount() == 0
                || !(getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount() - 1) instanceof TotalsColumn)) {
            addColumn(new TotalsColumn(TOTAL_COLUMN_STYLE));
        }
    }
    
    private void ensureTotalRacesSailedColumn() {
        // add a totals column on the right 
        if (getLeaderboardTable().getColumnCount() == 0
                || !(getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount() - 2) instanceof TotalRacesCompletedColumn)) {
            insertColumn(getLeaderboardTable().getColumnCount() - 1, new TotalRacesCompletedColumn(TOTAL_COLUMN_STYLE));
        }
    }
    
    private void ensureNoTotalRacesSailedColumn() {
        if ((getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount() - 2) instanceof TotalRacesCompletedColumn)) {
            removeColumn(getLeaderboardTable().getColumnCount() - 2);
        }
    }
    
    protected void updateTotalRacesSailedColumn() {
        final boolean showTotalRacesCompletedColumn = isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor();
        if (showTotalRacesCompletedColumn) {
            ensureTotalRacesSailedColumn();
        } else {
            ensureNoTotalRacesSailedColumn();
        }
    }

    /**
     * If the <code>leaderboard</code> {@link LeaderboardDTO#hasCarriedPoints has carried points} and if column #1
     * (second column, right of the competitor column) does not exist or is not of type {@link CarryColumn}, all columns
     * starting from #1 will be removed and a {@link CarryColumn} will be added. If the leaderboard has no carried
     * points but the display still shows a carry column, the column is removed.
     * 
     * @param zeroBasedIndexOfCarryColumn
     *            the 0-based column index where to put the carry column, if any
     * @return the 0-based index of the next column following this column if the carry column is to be shown, or
     *         <code>zeroBasedIndexOfCarryColumn</code> otherwise
     */
    protected int updateCarryColumn(LeaderboardDTO leaderboard, int zeroBasedIndexOfCarryColumn) {
        final boolean needsCarryColumn = leaderboard != null && leaderboard.hasCarriedPoints;
        if (needsCarryColumn) {
            ensureCarryColumn(zeroBasedIndexOfCarryColumn);
        } else {
            ensureNoCarryColumn(zeroBasedIndexOfCarryColumn);
        }
        return needsCarryColumn ? zeroBasedIndexOfCarryColumn+1 : zeroBasedIndexOfCarryColumn;
    }

    private void ensureNoCarryColumn(int zeroBasedIndexOfCarryColumn) {
        if (getLeaderboardTable().getColumnCount() > zeroBasedIndexOfCarryColumn
                && getLeaderboardTable().getColumn(zeroBasedIndexOfCarryColumn) instanceof CarryColumn) {
            removeColumn(zeroBasedIndexOfCarryColumn);
        }
    }

    protected void ensureCarryColumn(int zeroBasedIndexOfCarryColumn) {
        if (getLeaderboardTable().getColumnCount() <= zeroBasedIndexOfCarryColumn
                || !(getLeaderboardTable().getColumn(zeroBasedIndexOfCarryColumn) instanceof CarryColumn)) {
            while (getLeaderboardTable().getColumnCount() > zeroBasedIndexOfCarryColumn) {
                removeColumn(zeroBasedIndexOfCarryColumn);
            }
            addColumn(createCarryColumn());
        }
    }

    protected CarryColumn createCarryColumn() {
        return new CarryColumn();
    }

    protected SortedCellTable<LeaderboardRowDTO> getLeaderboardTable() {
        return leaderboardTable;
    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    protected String getLeaderboardName() {
        return leaderboardName;
    }

    protected void setLeaderboardName(String leaderboardName) {
        this.leaderboardName = leaderboardName;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    protected ListDataProvider<LeaderboardRowDTO> getData() {
        return getLeaderboardTable().getDataProvider();
    }

    /**
     * @param newTime
     *            ignored; may be <code>null</code>. The time for loading the leaderboard is determined using
     *            {@link #getLeaderboardDisplayDate()}.
     */
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadCompleteLeaderboard(getLeaderboardDisplayDate());
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        currentlyHandlingPlayStateChange = true;
        
        if(!isEmbedded) {
            playPause.setHTML(getPlayPauseImgHtml(playState));
            playPause.setTitle(playState == PlayStates.Playing ? stringMessages.pauseAutomaticRefresh() : stringMessages.autoRefresh());
        }
        if (!settingsUpdatedExplicitly && playMode != oldPlayMode) {
            // if settings weren't explicitly modified, auto-switch to live mode settings and sort for
            // any pre-selected race; we need to copy the previously selected race columns to the new RaceColumnSelection
            updateSettings(LeaderboardSettingsFactory.getInstance().createNewSettingsForPlayMode(
                    playMode,
                    /* don't touch columnToSort if no race was pre-selected */ preSelectedRace == null ? null
                            : preSelectedRace.getRaceName(),
                    /* don't change nameOfRaceColumnToShow */null,
                    /* set nameOfRaceToShow if race was pre-selected */preSelectedRace == null ? null : preSelectedRace
                            .getRaceName(), getRaceColumnSelection(), /* leave showRegattaRank and overall details unchanged */ null,
                            /* take into account state of competitor columns*/isShowCompetitorSailId(), isShowCompetitorFullName()));
        }
        currentlyHandlingPlayStateChange = false;
        oldPlayMode = playMode;
    }

    @Override
    public void playSpeedFactorChanged(double newPlaySpeedFactor) {
        // nothing to do
    }

    private List<RegattaAndRaceIdentifier> getTrackedRacesIdentifiers() {
        List<RegattaAndRaceIdentifier> result = new ArrayList<RegattaAndRaceIdentifier>();
        for (RaceColumnDTO raceColumn : getLeaderboard().getRaceList()) {
            for (FleetDTO fleet : raceColumn.getFleets()) {
                if (raceColumn.getRaceIdentifier(fleet) != null) {
                    result.add(raceColumn.getRaceIdentifier(fleet));
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public boolean hasToolbar() {
        return false;
    }

    @Override
    public SettingsDialogComponent<LeaderboardSettings> getSettingsDialogComponent() {
        return new LeaderboardSettingsDialogComponent(Collections.unmodifiableList(selectedManeuverDetails),
                Collections.unmodifiableList(selectedLegDetails), Collections.unmodifiableList(selectedRaceDetails),
                Collections.unmodifiableList(selectedOverallDetailColumns), /* All races to select */ leaderboard.getRaceList(),
                raceColumnSelection.getSelectedRaceColumnsOrderedAsInLeaderboard(leaderboard), raceColumnSelection, autoExpandPreSelectedRace,
                isShowAddedScores(), timer.getRefreshInterval(), isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(), 
                isShowCompetitorSailId(), isShowCompetitorFullName(), stringMessages);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    private LeaderboardRowDTO getRow(CompetitorDTO competitor) {
        synchronized (getData().getList()) {
            for (LeaderboardRowDTO row : getData().getList()) {
                if (row.competitor.equals(competitor)) {
                    return row;
                }
            }
        }
        return null;
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        LeaderboardRowDTO row = getRow(competitor);
        if (row != null) {
            leaderboardSelectionModel.setSelected(row, true);
        }
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        LeaderboardRowDTO row = getRow(competitor);
        if (row != null) {
            leaderboardSelectionModel.setSelected(row, false);
        }
    }

    @Override
    public BusyIndicator getBusyIndicator() {
        return busyIndicator;
    }

    @Override
    public boolean hasBusyIndicator() {
        return true;
    }
    
    private Iterable<LeaderboardRowDTO> getSelectedRows() {
        return leaderboardSelectionModel.getSelectedSet();
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        setFilterControlStatus();
        if (timer.isInitialized()) {
            timeChanged(timer.getTime(), null);
        }
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
        setFilterControlStatus();
        updateLeaderboard(getLeaderboard());
    }

    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
        // nothing to do; if the list of filtered competitors has changed, a separate call to filteredCompetitorsListChanged will occur
        setFilterControlStatus();
    }

    public RaceColumnSelection getRaceColumnSelection() {
        return raceColumnSelection;
    }
    
    public void removeAllListeners() {
        if (raceTimesInfoProviderListener != null) {
            getRaceTimesInfoProvider().removeRaceTimesInfoProviderListener(raceTimesInfoProviderListener);
        }
        if (raceColumnSelection != null && raceColumnSelection.getType() == RaceColumnSelectionStrategies.LAST_N) {
            getRaceTimesInfoProvider().removeRaceTimesInfoProviderListener(
                    (LastNRacesColumnSelection) raceColumnSelection);
        }
        if (timer != null) {
            timer.removeTimeListener(this);
        }
        if(leaderboardUpdateListener != null) {
            leaderboardUpdateListener.clear();
        }
    }

    protected Timer getTimer() {
        return timer;
    }

    protected void blurFocusedElementAfterSelectionChange() {
        // now "blur" the selected leaderboard element because it seems to cause the cell table to scroll to its top;
        // see bug 2093.
        blur();
        final ScheduledCommand blurCommand = new ScheduledCommand() {
            @Override
            public void execute() {
                blur();
            }
        };
        Scheduler.get().scheduleDeferred(blurCommand);
    }

    private void blur() {
        if (elementToBlur != null) {
            elementToBlur.blur();
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            timeChanged(getLeaderboardDisplayDate(), null);
        }
    }
    
    public void addLeaderboardUpdateListener(LeaderboardUpdateListener listener) {
        this.leaderboardUpdateListener.add(listener);
    }

    protected void informLeaderboardUpdateListenersAboutLeaderboardUpdated(LeaderboardDTO leaderboard) {
        for (LeaderboardUpdateListener listener : this.leaderboardUpdateListener) {
            listener.updatedLeaderboard(leaderboard);
        }
    }
    
    protected void informLeaderboardUpdateListenersAboutRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        for (LeaderboardUpdateListener listener : this.leaderboardUpdateListener) {
           listener.currentRaceSelected(raceIdentifier, raceColumn);
        }
    }

    @Override
    public String getDependentCssClassName() {
        return "leaderboard";
    }
}
