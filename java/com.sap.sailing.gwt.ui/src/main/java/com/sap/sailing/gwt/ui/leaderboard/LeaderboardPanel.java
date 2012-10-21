package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetLeaderboardByNameAction;
import com.sap.sailing.gwt.ui.client.Collator;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.PlayStateListener;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.leaderboard.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.shared.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.LegEntryDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.IsEmbeddableComponent;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.panels.BusyIndicator;
import com.sap.sailing.gwt.ui.shared.panels.SimpleBusyIndicator;

/**
 * A leaderboard essentially consists of a table widget that in its columns displays the entries.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardPanel extends FormPanel implements TimeListener, PlayStateListener,
        Component<LeaderboardSettings>, IsEmbeddableComponent, CompetitorSelectionChangeListener, LeaderboardFetcher {
    private static final int RANK_COLUMN_INDEX = 0;

    private static final int SAIL_ID_COLUMN_INDEX = 1;

    private static final int CARRY_COLUMN_INDEX = 3;

    protected static final NumberFormat scoreFormat = NumberFormat.getFormat("0.00");

    private final SailingServiceAsync sailingService;

    /**
     * The leaderboard name is used to
     * {@link SailingServiceAsync#getLeaderboardByName(String, java.util.Date, String[], com.google.gwt.user.client.rpc.AsyncCallback)
     * obtain the leaderboard contents} from the server. It may change in case the leaderboard is renamed.
     */
    private String leaderboardName;

    private final ErrorReporter errorReporter;

    private final StringMessages stringMessages;

    private final SortedCellTable<LeaderboardRowDTO> leaderboardTable;

    private final SelectionModel<LeaderboardRowDTO> leaderboardSelectionModel;

    private LeaderboardDTO leaderboard;

    private final RankColumn rankColumn;

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
    
    private final Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> overallDetailColumnMap;

    private RaceColumnSelection raceColumnSelection;

    protected final String RACE_COLUMN_HEADER_STYLE;

    protected final String LEG_DETAIL_COLUMN_HEADER_STYLE;

    protected final String LEG_DETAIL_COLUMN_STYLE;

    protected final String LEG_COLUMN_HEADER_STYLE;

    protected final String RACE_COLUMN_STYLE;

    protected final String LEG_COLUMN_STYLE;

    protected final String TOTAL_COLUMN_STYLE;

    private final Timer timer;

    private boolean autoExpandPreSelectedRace;

    /**
     * Remembers whether the auto-expand of the pre-selected race (see {@link #autoExpandPreSelectedRace}) has been
     * performed once. It must not be performed another time.
     */
    private boolean autoExpandPerformedOnce;

    /**
     * This anchor's HTML holds the image tag for the play/pause button that needs to be updated when the {@link #timer}
     * changes its playing state
     */
    private final Anchor playPause;

    private final CompetitorSelectionProvider competitorSelectionProvider;

    /**
     * If this is <code>null</code>, all leaderboard columns added by updating the leaderboard from the server are
     * automatically added to the table. Otherwise, only the column whose
     * {@link RaceColumnDTO#getRaceIdentifier(String) race identifier} matches the value of this attribute will be
     * added.
     */
    private final RaceIdentifier preSelectedRace;

    private final VerticalPanel contentPanel;
    private final HorizontalPanel refreshAndSettingsPanel;

    private final FlowPanel informationPanel;
    private final Label scoreCorrectionLastUpdateTimeLabel;
    private final Label scoreCorrectionCommentLabel;

    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG);
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private boolean isEmbedded = false;

    private static LeaderboardResources resources = GWT.create(LeaderboardResources.class);
    private static LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);

    private final ImageResource pauseIcon;
    private final ImageResource playIcon;

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

    private class SettingsClickHandler implements ClickHandler {
        private final StringMessages stringMessages;

        private SettingsClickHandler(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public void onClick(ClickEvent event) {
            new SettingsDialog<LeaderboardSettings>(LeaderboardPanel.this, stringMessages).show();
        }
    }

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
    public boolean isEmbedded() {
        return isEmbedded;
    }

    protected VerticalPanel getContentPanel() {
        return contentPanel;
    }

    public void updateSettings(LeaderboardSettings newSettings) {
        if (!newSettings.updateUponPlayStateChange() || !currentlyHandlingPlayStateChange) {
            settingsUpdatedExplicitly = true;
        }
        List<ExpandableSortableColumn<?>> columnsToExpandAgain = new ArrayList<ExpandableSortableColumn<?>>();
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
        if (newSettings.getOverallDetailsToShow() != null) {
            selectedOverallDetailColumns.clear();
            selectedOverallDetailColumns.addAll(newSettings.getOverallDetailsToShow());
        }
        if (newSettings.getNamesOfRaceColumnsToShow() != null) {
            raceColumnSelection.requestClear();
            for (String nameOfRaceColumnToShow : newSettings.getNamesOfRaceColumnsToShow()) {
                RaceColumnDTO raceColumnToShow = getRaceByColumnName(nameOfRaceColumnToShow);
                if (raceColumnToShow != null) {
                    raceColumnSelection.requestRaceColumnSelection(raceColumnToShow.name, raceColumnToShow);
                }
            }
        } else if (newSettings.getNamesOfRacesToShow() != null) {
            raceColumnSelection.requestClear();
            for (String nameOfRaceToShow : newSettings.getNamesOfRacesToShow()) {
                RaceColumnDTO raceColumnToShow = getRaceByName(nameOfRaceToShow);
                if (raceColumnToShow != null) {
                    raceColumnSelection.requestRaceColumnSelection(raceColumnToShow.name, raceColumnToShow);
                }
            }
        }
        setAutoExpandPreSelectedRace(false); // avoid expansion during updateLeaderboard(...); will expand later if it
                                             // was expanded before
        // update leaderboard after settings panel column selection change
        updateLeaderboard(leaderboard);
        setAutoExpandPreSelectedRace(newSettings.isAutoExpandPreSelectedRace());

        if (newSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setRefreshInterval(newSettings.getDelayBetweenAutoAdvancesInMilliseconds());
        }
        if (newSettings.getDelayInMilliseconds() != null) {
            setDelayInMilliseconds(newSettings.getDelayInMilliseconds());
        }
        for (ExpandableSortableColumn<?> expandableSortableColumn : columnsToExpandAgain) {
            expandableSortableColumn.toggleExpansion();
        }
        if (newSettings.getNameOfRaceToSort() != null) {
            final RaceColumn<?> raceColumnByRaceName = getRaceColumnByRaceName(newSettings.getNameOfRaceToSort());
            if (raceColumnByRaceName != null) {
                getLeaderboardTable().sortColumn(raceColumnByRaceName, /* ascending */true);
            }
        }
    }

    protected class CompetitorColumn extends SortableColumn<LeaderboardRowDTO, LeaderboardRowDTO> {
        private final CompetitorColumnBase<LeaderboardRowDTO> base;
        
        protected CompetitorColumn(CompetitorColumnBase<LeaderboardRowDTO> base) {
            super(base.getCell(getLeaderboard()), SortingOrder.ASCENDING);
            this.base = base;
        }

        public CompetitorColumn(CompositeCell<LeaderboardRowDTO> compositeCell, CompetitorColumnBase<LeaderboardRowDTO> base) {
            super(compositeCell, SortingOrder.ASCENDING);
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
        public Header<String> getHeader() {
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
            String competitorColor = LeaderboardPanel.this.competitorSelectionProvider.getColor(object.competitor);
            String competitorColorBarStyle;
            if (LeaderboardPanel.this.isEmbedded) {
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
    private class SailIDColumn<T> extends SortableColumn<T, String> {
        private final CompetitorFetcher<T> competitorFetcher;
        
        protected SailIDColumn(CompetitorFetcher<T> competitorFetcher) {
            super(new TextCell(), SortingOrder.ASCENDING);
            this.competitorFetcher = competitorFetcher;
        }

        @Override
        public InvertibleComparator<T> getComparator() {
            return new InvertibleComparatorAdapter<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return competitorFetcher.getCompetitor(o1).sailID == null ? competitorFetcher.getCompetitor(o2).sailID == null ? 0 : -1
                            : competitorFetcher.getCompetitor(o2).sailID == null ? 1 : Collator.getInstance().compare(
                                    competitorFetcher.getCompetitor(o1).sailID, competitorFetcher.getCompetitor(o2).sailID);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringMessages.competitor());
        }

        @Override
        public void render(Context context, T object, SafeHtmlBuilder sb) {
            ImageResourceRenderer renderer = new ImageResourceRenderer();
            ImageResource flagImageResource = FlagImageResolver
                    .getFlagImageResource(competitorFetcher.getCompetitor(object).twoLetterIsoCountryCode);
            if (flagImageResource != null) {
                sb.append(renderer.render(flagImageResource));
                sb.appendHtmlConstant("&nbsp;");
            }
            sb.appendEscaped(competitorFetcher.getCompetitor(object).sailID);
        }

        @Override
        public String getValue(T object) {
            return competitorFetcher.getCompetitor(object).sailID;
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
                    LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE, selectedRaceDetails);
            setHorizontalAlignment(ALIGN_CENTER);
            this.race = race;
            this.headerStyle = headerStyle;
            this.columnStyle = columnStyle;
        }

        public RaceColumnDTO getRace() {
            return race;
        }

        public String getRaceColumnName() {
            return race.getRaceColumnName();
        }

        public boolean isMedalRace() {
            return race.isMedalRace();
        }

        @Override
        public String getColumnStyle() {
            return columnStyle;
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
                String fleetColorBarStyle = "";
                String totalPointsAsText = entry.totalPoints == null ? "" : scoreFormat.format(entry.totalPoints);
                String netPointsAsText = entry.netPoints == null ? "" : scoreFormat.format(entry.netPoints);

                if (entry.fleet != null && entry.fleet.getColor() != null) {
                    fleetColorBarStyle = " style=\"border-bottom: 3px solid " + entry.fleet.getColor().getAsHtml()
                            + ";\"";
                }
                html.appendHtmlConstant("<div" + fleetColorBarStyle + ">");
                // don't show points if max points / penalty
                if (entry.reasonForMaxPoints == null || entry.reasonForMaxPoints == MaxPointsReason.NONE) {
                    if (!entry.discarded) {
                        html.appendHtmlConstant("<span style=\"font-weight: bold;\">");
                        html.appendHtmlConstant(totalPointsAsText);
                        html.appendHtmlConstant("</span>");
                    } else {
                        html.appendHtmlConstant(" <span style=\"opacity: 0.5;\"><del>");
                        html.appendHtmlConstant(netPointsAsText);
                        html.appendHtmlConstant("</del></span>");
                    }
                } else {
                    html.appendHtmlConstant(" <span title=\"" + netPointsAsText + "/" + totalPointsAsText
                            + "\" style=\"opacity: 0.5;\">");
                    if (entry.discarded) {
                        html.appendHtmlConstant("<del>");
                    }
                    html.appendEscaped(entry.reasonForMaxPoints == MaxPointsReason.NONE ? "" : entry.reasonForMaxPoints
                            .name());
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
                    List<CompetitorDTO> competitorsFromBestToWorst = getLeaderboard().getCompetitorsFromBestToWorst(
                            race);
                    int o1Rank = competitorsFromBestToWorst.indexOf(o1.competitor) + 1;
                    int o2Rank = competitorsFromBestToWorst.indexOf(o2.competitor) + 1;
                    return o1Rank == 0 ? o2Rank == 0 ? 0 : isAscending() ? 1 : -1 : o2Rank == 0 ? isAscending() ? -1
                            : 1 : o1Rank - o2Rank;
                }
            };
        }

        @Override
        public String getHeaderStyle() {
            return headerStyle;
        }

        @Override
        public Header<SafeHtml> getHeader() {
            SortableExpandableColumnHeader header = new SortableExpandableColumnHeader(
            /* title */race.getRaceColumnName(),
            /* iconURL */race.isMedalRace() ? "/gwt/images/medal_small.png" : null, LeaderboardPanel.this, this,
                    stringMessages);
            return header;
        }
    }

    public static DetailType[] getAvailableRaceDetailColumnTypes() {
        return new DetailType[] { DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_DISTANCE_TRAVELED, DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS, DetailType.NUMBER_OF_MANEUVERS, DetailType.DISPLAY_LEGS,
                DetailType.CURRENT_LEG, DetailType.RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS,
                DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS };
    }

    public static DetailType[] getAvailableOverallDetailColumnTypes() {
        return new DetailType[] { DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS,
                DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS,
                DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS,
                DetailType.TOTAL_TIME_SAILED_IN_SECONDS, DetailType.MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS };
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
            if (getLeaderboard().getLegCount(getRaceColumnName()) != -1) {
                callWhenExpansionDataIsLoaded.run();
            } else {
                getSailingService().getLeaderboardByName(getLeaderboardName(),
                        timer.getPlayMode() == PlayModes.Live ? null : getLeaderboardDisplayDate(),
                        /* namesOfRacesForWhichToLoadLegDetails */getNamesOfExpandedRaces(),
                        new AsyncCallback<LeaderboardDTO>() {
                            @Override
                            public void onSuccess(LeaderboardDTO result) {
                                updateLeaderboard(result);
                                callWhenExpansionDataIsLoaded.run();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                getErrorReporter().reportError(
                                        stringMessages.errorTryingToObtainLeaderboardContents(caught.getMessage()),
                                        true /* silentMode */);
                            }
                        });
            }
        }

        @Override
        protected Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> getDetailColumnMap(
                LeaderboardPanel leaderboardPanel, StringMessages stringMessages, String detailHeaderStyle,
                String detailColumnStyle) {
            Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> result = new HashMap<DetailType, SortableColumn<LeaderboardRowDTO, ?>>();
            result.put(
                    DetailType.RACE_DISTANCE_TRAVELED,
                    new FormattedDoubleLegDetailColumn(stringMessages.distanceInMeters(), "["
                            + stringMessages.distanceInMetersUnit() + "]", new RaceDistanceTraveledInMeters(),
                            DetailType.RACE_DISTANCE_TRAVELED.getPrecision(), DetailType.RACE_DISTANCE_TRAVELED
                                    .getDefaultSortingOrder(), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, new FormattedDoubleLegDetailColumn(
                    stringMessages.averageSpeedInKnots(), "[" + stringMessages.averageSpeedInKnotsUnit() + "]",
                    new RaceAverageSpeedInKnots(), DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS.getPrecision(),
                    DetailType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS.getDefaultSortingOrder(),
                    LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(
                    DetailType.RACE_GAP_TO_LEADER_IN_SECONDS,
                    new FormattedDoubleLegDetailColumn(stringMessages.gapToLeaderInSeconds(), "["
                            + stringMessages.gapToLeaderInSecondsUnit() + "]", new RaceGapToLeaderInSeconds(),
                            DetailType.RACE_GAP_TO_LEADER_IN_SECONDS.getPrecision(),
                            DetailType.RACE_GAP_TO_LEADER_IN_SECONDS.getDefaultSortingOrder(), LEG_COLUMN_HEADER_STYLE,
                            LEG_COLUMN_STYLE));
            result.put(
                    DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                    new FormattedDoubleLegDetailColumn(stringMessages.currentSpeedOverGroundInKnots(), "["
                            + stringMessages.currentSpeedOverGroundInKnotsUnit() + "]", new CurrentSpeedOverGroundInKnots(),
                            DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS.getPrecision(),
                            DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS.getDefaultSortingOrder(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(
                    DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS,
                    new FormattedDoubleLegDetailColumn(stringMessages.windwardDistanceToLeader(), "["
                            + stringMessages.distanceInMetersUnit() + "]", new RaceDistanceToLeaderInMeters(),
                            DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS.getPrecision(),
                            DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS.getDefaultSortingOrder(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(DetailType.RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS,
                    new FormattedDoubleLegDetailColumn(stringMessages.averageCrossTrackErrorInMeters(), "["
                            + stringMessages.metersUnit() + "]", new RaceAverageCrossTrackErrorInMeters(),
                            DetailType.RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS.getPrecision(),
                            DetailType.RACE_AVERAGE_CROSS_TRACK_ERROR_IN_METERS.getDefaultSortingOrder(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(DetailType.NUMBER_OF_MANEUVERS, getManeuverCountRaceColumn());
            result.put(DetailType.CURRENT_LEG,
                    new FormattedDoubleLegDetailColumn(stringMessages.currentLeg(), "", new CurrentLeg(),
                            DetailType.CURRENT_LEG.getPrecision(), DetailType.CURRENT_LEG.getDefaultSortingOrder(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));

            return result;
        }

        private ManeuverCountRaceColumn getManeuverCountRaceColumn() {
            return new ManeuverCountRaceColumn(getLeaderboardPanel(), this, stringMessages,
                    LeaderboardPanel.this.selectedManeuverDetails, LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE,
                    LEG_DETAIL_COLUMN_HEADER_STYLE, LEG_DETAIL_COLUMN_STYLE);
        }

        @Override
        protected Iterable<SortableColumn<LeaderboardRowDTO, ?>> getDirectChildren() {
            List<SortableColumn<LeaderboardRowDTO, ?>> result = new ArrayList<SortableColumn<LeaderboardRowDTO, ?>>();
            for (SortableColumn<LeaderboardRowDTO, ?> column : super.getDirectChildren()) {
                result.add(column);
            }
            if (isExpanded() && selectedRaceDetails.contains(DetailType.DISPLAY_LEGS)) {
                // it is important to re-use existing LegColumn objects because
                // removing the columns from the table
                // is based on column identity
                int legCount = getLeaderboard().getLegCount(getRaceColumnName());
                if (legCount != -1) {
                    for (int i = 0; i < legCount; i++) {
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
                            distanceTraveledInMeters += legDetail.distanceTraveledInMeters;
                            timeInMilliseconds += legDetail.timeInMilliseconds;
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
         * Fetches the average cross-track error for the race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceAverageCrossTrackErrorInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDTO row) {
                Double result = null;
                LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceColumnName());
                if (fieldsForRace != null) {
                    result = fieldsForRace.averageCrossTrackErrorInMeters;
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
                    LegEntryDTO lastLegDetail = fieldsForRace.legDetails.get(lastLegIndex);
                    // competitor may be in leg prior to the one the leader is in; find competitors current leg
                    while (lastLegDetail == null && lastLegIndex > 0) {
                        lastLegDetail = fieldsForRace.legDetails.get(--lastLegIndex);
                    }
                    if (lastLegDetail != null) {
                        result = lastLegDetail.currentSpeedOverGroundInKnots;
                    }
                }
                return result;
            }
        }
    }

    /**
     * Displays the totals for a competitor for the entire leaderboard.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    private class TotalsColumn extends SortableColumn<LeaderboardRowDTO, String> {
        private final String columnStyle;

        protected TotalsColumn(String columnStyle) {
            super(new TextCell(), SortingOrder.ASCENDING);
            this.columnStyle = columnStyle;
            setHorizontalAlignment(ALIGN_CENTER);
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            Double totalPoints = getLeaderboard().getTotalPoints(object);
            return "" + (totalPoints == null ? "" : scoreFormat.format(totalPoints));
        }

        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<span style=\"font-weight: bold;\">");
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
        public Header<String> getHeader() {
            return new TextHeader(stringMessages.total());
        }
    }

    protected class CarryColumn extends SortableColumn<LeaderboardRowDTO, String> {
        public CarryColumn() {
            super(new TextCell(), SortingOrder.ASCENDING);
            setSortable(true);
        }

        protected CarryColumn(EditTextCell editTextCell) {
            super(editTextCell, SortingOrder.ASCENDING);
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            return object.carriedPoints == null ? "" : scoreFormat.format(object.carriedPoints);
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
        public Header<String> getHeader() {
            return new TextHeader(stringMessages.carry());
        }
    }

    private class RankColumn extends SortableColumn<LeaderboardRowDTO, String> {
        public RankColumn() {
            super(new TextCell(), SortingOrder.ASCENDING);
            setHorizontalAlignment(ALIGN_CENTER);
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            final int rank = getLeaderboard().getRank(object.competitor);
            return "" + (rank == 0 ? "" : rank);
        }

        @Override
        public InvertibleComparator<LeaderboardRowDTO> getComparator() {
            return new InvertibleComparatorAdapter<LeaderboardRowDTO>() {
                @Override
                public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                    final int rank1 = getLeaderboard().getRank(o1.competitor);
                    final int rank2 = getLeaderboard().getRank(o2.competitor);
                    return rank1 == 0 ? rank2 == 0 ? 0 : 1 : rank2 == 0 ? -1 : rank1 - rank2;
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringMessages.totalRank());
        }
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, CompetitorSelectionProvider competitorSelectionProvider,
            String leaderboardName, String leaderboardGroupName, ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails) {
        this(sailingService, asyncActionsExecutor, settings, /* preSelectedRace */null, competitorSelectionProvider,
                leaderboardName, leaderboardGroupName, errorReporter, stringMessages, userAgent, showRaceDetails);
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, RaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, String leaderboardName,
            String leaderboardGroupName, ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent, boolean showRaceDetails) {
        this(sailingService, asyncActionsExecutor, settings, preSelectedRace, competitorSelectionProvider, new Timer(
                PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */3000l), leaderboardName,
                leaderboardGroupName, errorReporter, stringMessages, userAgent, showRaceDetails,
                preSelectedRace == null ? new ExplicitRaceColumnSelection() : new ExplicitRaceColumnSelectionWithPreselectedRace(preSelectedRace));
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings settings, RaceIdentifier preSelectedRace,
            CompetitorSelectionProvider competitorSelectionProvider, Timer timer, String leaderboardName,
            String leaderboardGroupName, ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent, boolean showRaceDetails, RaceColumnSelection raceColumnSelection) {
        this.showRaceDetails = showRaceDetails;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.preSelectedRace = preSelectedRace;
        this.competitorSelectionProvider = competitorSelectionProvider;
        competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
        this.setLeaderboardName(leaderboardName);
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.selectedLegDetails = new ArrayList<DetailType>();
        this.selectedRaceDetails = new ArrayList<DetailType>();
        this.selectedOverallDetailColumns = new ArrayList<DetailType>();
        this.raceColumnSelection = raceColumnSelection;
        this.selectedManeuverDetails = new ArrayList<DetailType>();
        overallDetailColumnMap = createOverallDetailColumnMap();
        settingsUpdatedExplicitly = !settings.updateUponPlayStateChange();
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
        if (settings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setRefreshInterval(settings.getDelayBetweenAutoAdvancesInMilliseconds());
        }

        this.timer = timer;
        timer.addPlayStateListener(this);
        timer.addTimeListener(this);
        rankColumn = new RankColumn();
        RACE_COLUMN_HEADER_STYLE = tableResources.cellTableStyle().cellTableRaceColumnHeader();
        LEG_COLUMN_HEADER_STYLE = tableResources.cellTableStyle().cellTableLegColumnHeader();
        LEG_DETAIL_COLUMN_HEADER_STYLE = tableResources.cellTableStyle().cellTableLegDetailColumnHeader();
        RACE_COLUMN_STYLE = tableResources.cellTableStyle().cellTableRaceColumn();
        LEG_COLUMN_STYLE = tableResources.cellTableStyle().cellTableLegColumn();
        LEG_DETAIL_COLUMN_STYLE = tableResources.cellTableStyle().cellTableLegDetailColumn();
        TOTAL_COLUMN_STYLE = tableResources.cellTableStyle().cellTableTotalColumn();
        leaderboardTable = new SortedCellTableWithStylableHeaders<LeaderboardRowDTO>(
        /* pageSize */10000, tableResources);
        getLeaderboardTable().setWidth("100%");
        if (userAgent.isMobile() == UserAgentDetails.PlatformTypes.MOBILE) {
            leaderboardSelectionModel = new ToggleSelectionModel<LeaderboardRowDTO>();
        } else {
            leaderboardSelectionModel = new MultiSelectionModel<LeaderboardRowDTO>();
        }
        leaderboardSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<CompetitorDTO> selection = new ArrayList<CompetitorDTO>();
                for (LeaderboardRowDTO row : getSelectedRows()) {
                    selection.add(row.competitor);
                }
                LeaderboardPanel.this.competitorSelectionProvider.setSelection(selection,
                /* listenersNotToNotify */LeaderboardPanel.this);
            }
        });
        leaderboardTable.setSelectionModel(leaderboardSelectionModel);
        loadCompleteLeaderboard(getLeaderboardDisplayDate());

        if (this.preSelectedRace == null) {
            isEmbedded = false;
        } else {
            isEmbedded = true;
        }
        contentPanel = new VerticalPanel();
        contentPanel.setStyleName("leaderboardContent");
        informationPanel = new FlowPanel();
        informationPanel.setStyleName("leaderboardInfo");
        scoreCorrectionLastUpdateTimeLabel = new Label("");
        scoreCorrectionCommentLabel = new Label("");
        informationPanel.add(scoreCorrectionCommentLabel);
        informationPanel.add(scoreCorrectionLastUpdateTimeLabel);

        DockPanel toolbarPanel = new DockPanel();
        toolbarPanel.setStyleName("leaderboardContent-toolbar");
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
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
        ImageResource chartIcon = resources.chartIcon();
        ImageResource leaderboardSettingsIcon = resources.leaderboardSettingsIcon();
        pauseIcon = resources.pauseIcon();
        playIcon = resources.playIcon();
        refreshAndSettingsPanel = new HorizontalPanel();
        refreshAndSettingsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        HorizontalPanel refreshPanel = new HorizontalPanel();
        refreshPanel.setSpacing(5);
        refreshPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        refreshPanel.addStyleName("refreshPanel");
        toolbarPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        toolbarPanel.addStyleName("refreshAndSettings");
        playPause = new Anchor(getPlayPauseImgHtml(timer.getPlayState()));
        playPause.addClickHandler(playPauseHandler);
        playStateChanged(timer.getPlayState(), timer.getPlayMode());
        refreshPanel.add(playPause);
        Anchor chartsAnchor = new Anchor(AbstractImagePrototype.create(chartIcon).getSafeHtml());
        chartsAnchor.setTitle(stringMessages.showCharts());
        chartsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showCompareCompetitorsDialog();
            }
        });
        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(leaderboardSettingsIcon).getSafeHtml());
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new SettingsClickHandler(stringMessages));
        refreshAndSettingsPanel.add(chartsAnchor);
        refreshAndSettingsPanel.add(refreshPanel);
        refreshAndSettingsPanel.add(settingsAnchor);
        toolbarPanel.add(refreshAndSettingsPanel, DockPanel.EAST);
        if (!isEmbedded) {
            contentPanel.add(toolbarPanel);
        }
        contentPanel.add(getLeaderboardTable());
        setWidget(contentPanel);
        raceNameForDefaultSorting = settings.getNameOfRaceToSort();
    }

    private Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> createOverallDetailColumnMap() {
        Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> result = new HashMap<DetailType, SortableColumn<LeaderboardRowDTO, ?>>();
        result.put(DetailType.MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS, new MaxSpeedOverallColumn(stringMessages,
                RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE));
        result.put(DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS, new KingOfTheUpwindColumn(stringMessages,
                RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE));
        result.put(DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS, new KingOfTheDownwindColumn(stringMessages,
                RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE));
        result.put(DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS, new KingOfTheReachingColumn(stringMessages,
                RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE));
        result.put(DetailType.TOTAL_TIME_SAILED_IN_SECONDS, new TotalTimeSailedColumn(stringMessages,
                RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE));
        return result;
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
        timer.setLivePlayDelayInMillis(delayInMilliseconds);
    }

    public boolean isAutoExpandPreSelectedRace() {
        return autoExpandPreSelectedRace;
    }

    private void setAutoExpandPreSelectedRace(boolean autoExpandPreSelectedRace) {
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        if (autoExpandPreSelectedRace) {
            autoExpandPerformedOnce = false;
        }
    }

    /**
     * The time point for which the leaderboard currently shows results. In {@link PlayModes#Replay replay mode} this is
     * the {@link #timer}'s time point. In {@link PlayModes#Live live mode} the {@link #timer}'s time is quantizes to
     * the closest full second to increase the likelihood of cache hits in the back end.
     */
    protected Date getLeaderboardDisplayDate() {
        return timer.getTime();
    }

    /**
     * adds the <code>column</code> to the right end of the {@link #getLeaderboardTable() leaderboard table} and sets
     * the column style according to the {@link SortableColumn#getColumnStyle() column's style definition}.
     */
    protected void addColumn(SortableColumn<LeaderboardRowDTO, ?> column) {
        leaderboardTable.addColumn(column, column.getHeader(), column.getComparator(), column
                .getPreferredSortingOrder().isAscending());
        String columnStyle = column.getColumnStyle();
        if (columnStyle != null) {
            getLeaderboardTable().addColumnStyleName(getLeaderboardTable().getColumnCount() - 1, columnStyle);
        }
    }

    protected void insertColumn(int beforeIndex, SortableColumn<LeaderboardRowDTO, ?> column) {
        // remove column styles of those columns whose index will shift right by
        // one:
        removeColumnStyles(beforeIndex);
        getLeaderboardTable().insertColumn(beforeIndex, column, column.getHeader(), column.getComparator(),
                column.getPreferredSortingOrder().isAscending());
        addColumnStyles(beforeIndex);
    }

    private void addColumnStyles(int startColumn) {
        for (int i = startColumn; i < getLeaderboardTable().getColumnCount(); i++) {
            SortableColumn<LeaderboardRowDTO, ?> columnToRemoveStyleFor = (SortableColumn<LeaderboardRowDTO, ?>) getLeaderboardTable()
                    .getColumn(i);
            String columnStyle = columnToRemoveStyleFor.getColumnStyle();
            if (columnStyle != null) {
                getLeaderboardTable().addColumnStyleName(i, columnStyle);
            }
        }
    }

    private void removeColumnStyles(int startColumn) {
        for (int i = startColumn; i < getLeaderboardTable().getColumnCount(); i++) {
            SortableColumn<LeaderboardRowDTO, ?> columnToRemoveStyleFor = (SortableColumn<LeaderboardRowDTO, ?>) getLeaderboardTable()
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
                    getLeaderboardName(), timer.getPlayMode() == PlayModes.Live ? null : date,
                    /* namesOfRacesForWhichToLoadLegDetails */getNamesOfExpandedRaces(),
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
            asyncActionsExecutor.execute(getLeaderboardByNameAction);
        } else {
            getBusyIndicator().setBusy(false);
        }
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
            raceColumnSelection.autoUpdateRaceColumnSelectionForUpdatedLeaderboard(getLeaderboard(), leaderboard);
            setLeaderboard(leaderboard);
            adjustColumnLayout(leaderboard);
            for (RaceColumn<?> columnToCollapseAndExpandAgain : columnsToCollapseAndExpandAgain) {
                columnToCollapseAndExpandAgain.toggleExpansion();
            }
            adjustDelayToLive();
            getData().getList().clear();
            getData().getList().addAll(getRowsToDisplay(leaderboard));
            for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
                SortableColumn<?, ?> c = (SortableColumn<?, ?>) getLeaderboardTable().getColumn(i);
                c.updateMinMax(leaderboard);
                // Toggle pre-selected race, if the setting is set and it isn't open yet
                if (!autoExpandPerformedOnce && isAutoExpandPreSelectedRace() && c instanceof RaceColumn<?>
                        && ((RaceColumn<?>) c).getRace().hasTrackedRace(preSelectedRace)) {
                    ExpandableSortableColumn<?> expandableSortableColumn = (ExpandableSortableColumn<?>) c;
                    if (!expandableSortableColumn.isExpanded()) {
                        expandableSortableColumn.toggleExpansion();
                        autoExpandPerformedOnce = true;
                    }
                }
            }
            if (leaderboardTable.getCurrentlySortedColumn() != null) {
                leaderboardTable.sort();
            } else {
                SortableColumn<LeaderboardRowDTO, ?> columnToSortFor = getDefaultSortColumn();
                leaderboardTable.sortColumn(columnToSortFor, columnToSortFor.getPreferredSortingOrder().isAscending());
            }
            // Reselect the selected rows
            clearSelection();
            for (LeaderboardRowDTO row : getLeaderboardTable().getDataProvider().getList()) {
                if (competitorSelectionProvider.isSelected(row.competitor)) {
                    leaderboardSelectionModel.setSelected(row, true);
                }
            }
            scoreCorrectionCommentLabel.setText(leaderboard.getComment() != null ? leaderboard.getComment() : "");
            if (leaderboard.getTimePointOfLastCorrectionsValidity() != null) {
                Date lastCorrectionDate = leaderboard.getTimePointOfLastCorrectionsValidity();
                String lastUpdate = dateFormatter.format(lastCorrectionDate) + " "
                        + timeFormatter.format(lastCorrectionDate);
                scoreCorrectionLastUpdateTimeLabel.setText(stringMessages.lastScoreUpdate() + ": " + lastUpdate);
            } else {
                scoreCorrectionLastUpdateTimeLabel.setText("");
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
                    // If the new leaderboard no longer contains the column, getLegCount will return -1, causing the
                    // column
                    // to be collapsed if it was expanded. This is correct because otherwise, removing it would no
                    // longer
                    // know the correct leg count.
                    if (!rc.isTogglingInProcess() && rc.isExpanded()) {
                        int oldLegCount = getLeaderboard().getLegCount(rc.getRaceColumnName());
                        int newLegCount = newLeaderboard.getLegCount(rc.getRaceColumnName());
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
     * {@link RaceColumnDTO#getStartDate(FleetDTO) start time}, if no {@link #settingsUpdatedExplicitly explicit setting
     * changes} were performed, automatically adjusts the delay accordingly.
     */
    private void adjustDelayToLive() {
        if (!settingsUpdatedExplicitly && leaderboard.getDelayToLiveInMillisForLatestRace() != null) {
            setDelayInMilliseconds(leaderboard.getDelayToLiveInMillisForLatestRace());
        }
    }

    /**
     * Extracts the rows to display of the <code>leaderboard</code>. These are all {@link AbstractLeaderboardDTO#rows
     * rows} in case {@link #preSelectedRace} is <code>null</code>, or only the rows of the competitors who scored in
     * the race identified by {@link #preSelectedRace} otherwise.
     */
    private Collection<LeaderboardRowDTO> getRowsToDisplay(LeaderboardDTO leaderboard) {
        Collection<LeaderboardRowDTO> result;
        if (preSelectedRace == null) {
            result = leaderboard.rows.values();
        } else {
            result = new ArrayList<LeaderboardRowDTO>();
            for (CompetitorDTO competitorInPreSelectedRace : getCompetitors(preSelectedRace)) {
                result.add(leaderboard.rows.get(competitorInPreSelectedRace));
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

    private SortableColumn<LeaderboardRowDTO, ?> getDefaultSortColumn() {
        SortableColumn<LeaderboardRowDTO, ?> defaultSortColumn = null;
        if (raceNameForDefaultSorting != null) {
            defaultSortColumn = getRaceColumnByRaceName(raceNameForDefaultSorting);
        }
        if (defaultSortColumn == null) {
            defaultSortColumn = getRankColumn();
        }
        return defaultSortColumn;
    }

    private void clearSelection() {
        for (LeaderboardRowDTO row : getData().getList()) {
            leaderboardSelectionModel.setSelected(row, false);
        }
    }

    private RankColumn getRankColumn() {
        return rankColumn;
    }

    protected void setLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public LeaderboardDTO getLeaderboard() {
        return leaderboard;
    }

    private void adjustColumnLayout(LeaderboardDTO leaderboard) {
        ensureRankColumn();
        ensureSailIDAndCompetitorColumn();
        boolean hasCarryColumn = updateCarryColumn(leaderboard);
        adjustOverallDetailColumns(leaderboard, hasCarryColumn?CARRY_COLUMN_INDEX+1:CARRY_COLUMN_INDEX);
        // first remove race columns no longer needed:
        removeUnusedRaceColumns(leaderboard);
        if (leaderboard != null) {
            createMissingAndAdjustExistingRaceColumns(leaderboard);
            ensureTotalsColumn();
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
        List<SortableColumn<LeaderboardRowDTO, ?>> overallDetailColumnsToShow = new ArrayList<SortableColumn<LeaderboardRowDTO,?>>();
        // ensure the ordering in overallDetailColumnsToShow conforms to the ordering of getAvailableOverallDetailColumnTypes()
        for (DetailType overallDetailType : getAvailableOverallDetailColumnTypes()) {
            if (selectedOverallDetailColumns.contains(overallDetailType)) {
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
     * If header information doesn't match the race column's actual state (tracked races attached meaning expanable;
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
                    : getRaceColumnByRaceColumnName(selectedRaceColumn.name);
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

    private boolean shallExpandRaceColumn(RaceColumnDTO raceColumnDTO) {
        return showRaceDetails && raceColumnDTO.hasTrackedRaces();
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

    private void ensureRankColumn() {
        if (getLeaderboardTable().getColumnCount() == RANK_COLUMN_INDEX) {
            addColumn(getDefaultSortColumn());
        } else {
            if (!(getLeaderboardTable().getColumn(RANK_COLUMN_INDEX) instanceof RankColumn)) {
                throw new RuntimeException("The first column must always be the rank column but it was of type "
                        + getLeaderboardTable().getColumn(RANK_COLUMN_INDEX).getClass().getName());
            }
        }
    }

    private void ensureSailIDAndCompetitorColumn() {
        if (getLeaderboardTable().getColumnCount() <= SAIL_ID_COLUMN_INDEX) {
            addColumn(new SailIDColumn<LeaderboardRowDTO>(new CompetitorFetcher<LeaderboardRowDTO>() {
                @Override
                public CompetitorDTO getCompetitor(LeaderboardRowDTO t) {
                    return t.competitor;
                }
            }));
            addColumn(createCompetitorColumn());
        } else {
            if (!(getLeaderboardTable().getColumn(SAIL_ID_COLUMN_INDEX) instanceof SailIDColumn)) {
                throw new RuntimeException("The second column must always be the sail ID column but it was of type "
                        + getLeaderboardTable().getColumn(SAIL_ID_COLUMN_INDEX).getClass().getName());
            }
        }
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

    /**
     * If the <code>leaderboard</code> {@link LeaderboardDTO#hasCarriedPoints has carried points} and if column #1
     * (second column, right of the competitor column) does not exist or is not of type {@link CarryColumn}, all columns
     * starting from #1 will be removed and a {@link CarryColumn} will be added. If the leaderboard has no carried
     * points but the display still shows a carry column, the column is removed.
     * 
     * @return <code>true</code> if a carry column is now part of the table (regardless of whether it existed before),
     *         <code>false</code> otherwise
     */
    protected boolean updateCarryColumn(LeaderboardDTO leaderboard) {
        final boolean needsCarryColumn = leaderboard != null && leaderboard.hasCarriedPoints;
        if (needsCarryColumn) {
            ensureCarryColumn();
        } else {
            ensureNoCarryColumn();
        }
        return needsCarryColumn;
    }

    private void ensureNoCarryColumn() {
        if (getLeaderboardTable().getColumnCount() > CARRY_COLUMN_INDEX
                && getLeaderboardTable().getColumn(CARRY_COLUMN_INDEX) instanceof CarryColumn) {
            removeColumn(CARRY_COLUMN_INDEX);
        }
    }

    protected void ensureCarryColumn() {
        if (getLeaderboardTable().getColumnCount() <= CARRY_COLUMN_INDEX
                || !(getLeaderboardTable().getColumn(CARRY_COLUMN_INDEX) instanceof CarryColumn)) {
            while (getLeaderboardTable().getColumnCount() > CARRY_COLUMN_INDEX) {
                removeColumn(CARRY_COLUMN_INDEX);
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

    @Override
    public void timeChanged(Date date) {
        loadCompleteLeaderboard(getLeaderboardDisplayDate());
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        currentlyHandlingPlayStateChange = true;
        playPause.setHTML(getPlayPauseImgHtml(playState));
        playPause.setTitle(playState == PlayStates.Playing ? stringMessages.pauseAutomaticRefresh() : stringMessages
                .autoRefresh());
        if (!settingsUpdatedExplicitly && playMode != oldPlayMode) {
            // if settings weren't explicitly modified, auto-switch to live mode settings and sort for
            // any pre-selected race
            updateSettings(LeaderboardSettingsFactory.getInstance().createNewSettingsForPlayMode(
                    playMode,
                    /* don't touch columnToSort if no race was pre-selected */preSelectedRace == null ? null
                            : preSelectedRace.getRaceName(),
                    /* don't change nameOfRaceColumnToShow */null,
                    /* set nameOfRaceToShow if race was pre-selected */preSelectedRace == null ? null : preSelectedRace
                            .getRaceName()));
        }
        currentlyHandlingPlayStateChange = false;
        oldPlayMode = playMode;
    }

    private void showCompareCompetitorsDialog() {
        int selectedCompetitorsCount = Util.size(competitorSelectionProvider.getSelectedCompetitors());
        if (selectedCompetitorsCount < 1) {
            Window.alert(stringMessages.selectAtLeastOneCompetitor());
        } else {
            List<RegattaAndRaceIdentifier> races = getTrackedRacesIdentifiers();
            RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, errorReporter, races, 5000l /* requestInterval*/);
            CompareCompetitorsChartDialog chartDialog = new CompareCompetitorsChartDialog(sailingService, races, raceTimesInfoProvider,
                    competitorSelectionProvider, timer, stringMessages, errorReporter);
            chartDialog.show();
        }
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
                /* All races to select */
                Collections.unmodifiableList(selectedOverallDetailColumns), leaderboard.getRaceList(),
                raceColumnSelection.getSelectedRaceColumnsOrderedAsInLeaderboard(leaderboard), autoExpandPreSelectedRace, timer.getRefreshInterval(),
                timer.getLivePlayDelayInMillis(), stringMessages);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    private LeaderboardRowDTO getRow(CompetitorDTO competitor) {
        for (LeaderboardRowDTO row : getData().getList()) {
            if (row.competitor.equals(competitor)) {
                return row;
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
        ArrayList<LeaderboardRowDTO> selectedRows = new ArrayList<LeaderboardRowDTO>();
        for (LeaderboardRowDTO row : getData().getList()) {
            if (leaderboardSelectionModel.isSelected(row)) {
                selectedRows.add(row);
            }
        }
        return selectedRows;
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        timeChanged(timer.getTime());
    }
}
