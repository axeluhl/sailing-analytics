package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;

/**
 * A leaderboard essentially consists of a table widget that in its columns displays the entries.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardPanel extends FormPanel implements TimeListener, PlayStateListener {
    private static final int RANK_COLUMN_INDEX = 0;

    private static final int SAIL_ID_COLUMN_INDEX = 1;

    private static final int CARRY_COLUMN_INDEX = 3;

    private final SailingServiceAsync sailingService;

    /**
     * The leaderboard name is used to
     * {@link SailingServiceAsync#getLeaderboardByName(String, java.util.Date, String[], com.google.gwt.user.client.rpc.AsyncCallback)
     * obtain the leaderboard contents} from the server. It may change in case the leaderboard is renamed.
     */
    private String leaderboardName;

    private final ErrorReporter errorReporter;

    private final StringConstants stringConstants;

    private final CellTable<LeaderboardRowDAO> leaderboardTable;

    private ListDataProvider<LeaderboardRowDAO> data;

    private final ListHandler<LeaderboardRowDAO> listHandler;

    private LeaderboardDAO leaderboard;

    private final RankColumn rankColumn;

    private final List<DetailColumnType> selectedLegDetails;

    private final List<DetailColumnType> selectedRaceDetails;

    private List<String> selectedRaceColumns;

    protected final String RACE_COLUMN_HEADER_STYLE;

    protected final String LEG_DETAIL_COLUMN_HEADER_STYLE;

    protected final String LEG_DETAIL_COLUMN_STYLE;

    protected final String LEG_COLUMN_HEADER_STYLE;

    protected final String RACE_COLUMN_STYLE;

    protected final String LEG_COLUMN_STYLE;

    protected final String TOTAL_COLUMN_STYLE;

    private final Timer timer;
    
    boolean firstTimeLeaderboardLoaded;

    /**
     * The delay with which the timer shall work. Before the timer is resumed, the delay is set to this value.
     */
    private long delayInMilliseconds;

    /**
     * This anchor's HTML holds the image tag for the play/pause button that needs to be updated when the {@link #timer}
     * changes its playing state
     */
    private final Anchor playPause;
    private class SettingsClickHandler implements ClickHandler {
        private final StringConstants stringConstants;

        private SettingsClickHandler(StringConstants stringConstants) {
            this.stringConstants = stringConstants;
        }

        @Override
        public void onClick(ClickEvent event) {
            new LeaderboardSettingsPanel(Collections.unmodifiableList(selectedLegDetails),
                    Collections.unmodifiableList(selectedRaceDetails), /* All races to select */
                    leaderboard.getRaceList(), selectedRaceColumns, timer.getDelayBetweenAutoAdvancesInMilliseconds(),
                    stringConstants.leaderboardSettings(), stringConstants.selectLegDetails(), stringConstants.ok(),
                    stringConstants.cancel(), new Validator<LeaderboardSettingsPanel.Result>() {
                        @Override
                        public String getErrorMessage(LeaderboardSettingsPanel.Result valueToValidate) {
                            if (valueToValidate.getLegDetailsToShow().isEmpty()) {
                                return stringConstants.selectAtLeastOneLegDetail();
                            } else if (valueToValidate.getDelayBetweenAutoAdvancesInMilliseconds() < 1000) {
                                return stringConstants.chooseUpdateIntervalOfAtLeastOneSecond();
                            } else {
                                return null;
                            }
                        }
                    }, new AsyncCallback<LeaderboardSettingsPanel.Result>() {
                        @Override
                        public void onSuccess(LeaderboardSettingsPanel.Result result) {
                            List<ExpandableSortableColumn<?>> columnsToExpandAgain = new ArrayList<ExpandableSortableColumn<?>>();
                            for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
                                Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(i);
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
                            selectedLegDetails.clear();
                            selectedLegDetails.addAll(result.getLegDetailsToShow());
                            selectedRaceDetails.clear();
                            selectedRaceDetails.addAll(result.getRaceDetailsToShow());
                            selectedRaceColumns.clear();
                            selectedRaceColumns.addAll(result.getRaceColumnsToShow());
                            // update leaderboard after settings panel column selection change
                            updateLeaderboard(leaderboard);
                            
                            timer.setDelayBetweenAutoAdvancesInMilliseconds(result
                                    .getDelayBetweenAutoAdvancesInMilliseconds());
                            setDelayInMilliseconds(result.getDelayInMilliseconds());
                            for (ExpandableSortableColumn<?> expandableSortableColumn : columnsToExpandAgain) {
                                expandableSortableColumn.toggleExpansion();
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                        }
                    }, stringConstants, getDelayInMilliseconds()).show();
        }
    }

    public interface LeaderboardTableResources extends CellTable.Resources {
        interface LeaderboardTableStyle extends CellTable.Style {
            /**
             * Applied to header cells of race columns
             */
            String cellTableRaceColumnHeader();

            /**
             * Applied to header cells of race columns
             */
            String cellTableLegColumnHeader();

            /**
             * Applied to header cells of race columns
             */
            String cellTableLegDetailColumnHeader();

            /**
             * Applied to detail columns
             */
            String cellTableLegDetailColumn();

            /**
             * Applied to race columns
             */
            String cellTableRaceColumn();

            /**
             * Applied to leg columns
             */
            String cellTableLegColumn();

            /**
             * Applied to the totals columns
             */
            String cellTableTotalColumn();

        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "LeaderboardTable.css" })
        LeaderboardTableStyle cellTableStyle();
    }

    protected class CompetitorColumn extends SortableColumn<LeaderboardRowDAO, String> {

        protected CompetitorColumn() {
            super(new TextCell());
        }

        protected CompetitorColumn(EditTextCell editTextCell) {
            super(editTextCell);
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return Collator.getInstance().compare(getLeaderboard().getDisplayName(o1.competitor),
                            getLeaderboard().getDisplayName(o2.competitor));
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.name());
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return getLeaderboard().getDisplayName(object.competitor);
        }
    }

    /**
     * Shows the country flag and sail ID, if present
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    private class SailIDColumn extends SortableColumn<LeaderboardRowDAO, String> {

        protected SailIDColumn() {
            super(new TextCell());
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return o1.competitor.sailID == null ? o2.competitor.sailID == null ? 0 : -1
                            : o2.competitor.sailID == null ? 1 : Collator.getInstance().compare(o1.competitor.sailID,
                                    o2.competitor.sailID);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.competitor());
        }

        @Override
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<img title=\"" + object.competitor.countryName + "\" src=\""
                    + getFlagURL(object.competitor.twoLetterIsoCountryCode) + "\"/>&nbsp;");
            sb.appendEscaped(object.competitor.sailID);
        }

        private String getFlagURL(String twoLetterIsoCountryCode) {
            return "/images/flags/" + twoLetterIsoCountryCode.toLowerCase() + ".png";
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return object.competitor.sailID;
        }
    }

    /**
     * Displays net/total points and possible max-points reasons based on a {@link LeaderboardRowDAO} and a race name
     * and makes the column sortable by the total points.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    protected abstract class RaceColumn<C> extends ExpandableSortableColumn<C> {
        private final String raceName;
        private final boolean medalRace;

        private final String headerStyle;
        private final String columnStyle;

        public RaceColumn(String raceName, boolean medalRace, boolean enableExpansion, Cell<C> cell,
                String headerStyle, String columnStyle) {
            super(LeaderboardPanel.this, enableExpansion, cell, stringConstants, LEG_COLUMN_HEADER_STYLE,
                    LEG_COLUMN_STYLE, selectedRaceDetails);
            setHorizontalAlignment(ALIGN_CENTER);
            this.raceName = raceName;
            this.medalRace = medalRace;
            this.headerStyle = headerStyle;
            this.columnStyle = columnStyle;
        }

        public String getRaceName() {
            return raceName;
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
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder html) {
            LeaderboardEntryDAO entry = object.fieldsByRaceName.get(raceName);
            if (entry != null) {
                // don't show points if max points / penalty
                if (entry.reasonForMaxPoints.equals("NONE")) {
                    if (!entry.discarded) {
                        html.appendHtmlConstant("<span style=\"font-weight: bold;\">");
                        html.append(entry.totalPoints);
                        html.appendHtmlConstant("</span>");
                    } else {
                        html.appendHtmlConstant(" <span style=\"opacity: 0.5;\"><del>");
                        html.append(entry.netPoints);
                        html.appendHtmlConstant("</del></span>");
                    }
                } else {
                    html.appendHtmlConstant(" <span style=\"opacity: 0.5;\">");
                    if (entry.discarded) {
                        html.appendHtmlConstant("<del>");
                    }
                    html.appendEscaped(entry.reasonForMaxPoints);
                    if (entry.discarded) {
                        html.appendHtmlConstant("</del>");
                    }
                    html.appendHtmlConstant("</span>");
                }
            }
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            if (medalRace) {
                return getLeaderboard().getMedalRaceComparator(getRaceName());
            } else {
                return new Comparator<LeaderboardRowDAO>() {
                    @Override
                    public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                        boolean ascending = isSortedAscendingForThisColumn(getLeaderboardPanel().getLeaderboardTable());
                        LeaderboardEntryDAO o1Entry = o1.fieldsByRaceName.get(raceName);
                        LeaderboardEntryDAO o2Entry = o2.fieldsByRaceName.get(raceName);
                        return (o1Entry == null || o1Entry.netPoints == 0) ? (o2Entry == null || o2Entry.netPoints == 0) ? 0
                                : ascending ? 1 : -1
                                : (o2Entry == null || o2Entry.netPoints == 0) ? ascending ? -1 : 1 : o1Entry.netPoints
                                        - o2Entry.netPoints;
                    }
                };
            }
        }

        @Override
        public String getHeaderStyle() {
            return headerStyle;
        }

        @Override
        public Header<SafeHtml> getHeader() {
            SortableExpandableColumnHeader header = new SortableExpandableColumnHeader(
            /* title */raceName,
            /* iconURL */medalRace ? "/images/medal_small.png" : null, LeaderboardPanel.this, this, stringConstants);
            return header;
        }

        public boolean isMedalRace() {
            return medalRace;
        }

    }

    public static DetailColumnType[] getAvailableRaceDetailColumnTypes() {
        return new DetailColumnType[] { DetailColumnType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailColumnType.RACE_DISTANCE_TRAVELED, DetailColumnType.RACE_GAP_TO_LEADER_IN_SECONDS };
    }

    private class TextRaceColumn extends RaceColumn<String> {
        /**
         * Remembers the leg columns; <code>null</code>-padded, if {@link #getLegColumn(int)} asks for a column index
         * not yet existing. It is important to remember the columns because column removal happens based on identity.
         */
        private final List<LegColumn> legColumns;

        public TextRaceColumn(String raceName, boolean medalRace, boolean expandable, String headerStyle,
                String columnStyle) {
            super(raceName, medalRace, expandable, new TextCell(), headerStyle, columnStyle);
            legColumns = new ArrayList<LegColumn>();
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return "" + object.fieldsByRaceName.get(getRaceName()).totalPoints;
        }

        @Override
        protected void ensureExpansionDataIsLoaded(final Runnable callWhenExpansionDataIsLoaded) {
            if (getLeaderboard().getLegCount(getRaceName()) != -1) {
                callWhenExpansionDataIsLoaded.run();
            } else {
                getSailingService().getLeaderboardByName(getLeaderboardName(), getLeaderboardDisplayDate(),
                /* namesOfRacesForWhichToLoadLegDetails */getNamesOfExpandedRaces(),
                        new AsyncCallback<LeaderboardDAO>() {
                            @Override
                            public void onSuccess(LeaderboardDAO result) {
                                updateLeaderboard(result);
                                callWhenExpansionDataIsLoaded.run();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                getErrorReporter().reportError(
                                        "Error trying to obtain leaderboard contents: " + caught.getMessage());
                            }
                        });
            }
        }

        @Override
        protected Map<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>> getDetailColumnMap(
                LeaderboardPanel leaderboardPanel, StringConstants stringConstants, String detailHeaderStyle,
                String detailColumnStyle) {
            Map<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>> result = new HashMap<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>>();
            result.put(DetailColumnType.RACE_DISTANCE_TRAVELED,
                    new FormattedDoubleLegDetailColumn(stringConstants.distanceInMeters(),
                            new RaceDistanceTraveledInMeters(), 1, getLeaderboardPanel().getLeaderboardTable(),
                            LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(DetailColumnType.RACE_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, new FormattedDoubleLegDetailColumn(
                    stringConstants.averageSpeedInKnots(), new RaceAverageSpeedInKnots(), 1, getLeaderboardPanel()
                            .getLeaderboardTable(), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            result.put(DetailColumnType.RACE_GAP_TO_LEADER_IN_SECONDS, new FormattedDoubleLegDetailColumn(
                    stringConstants.gapToLeaderInSeconds(), new RaceGapToLeaderInSeconds(), 1, getLeaderboardPanel()
                            .getLeaderboardTable(), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE));
            return result;
        }

        @Override
        protected Iterable<SortableColumn<LeaderboardRowDAO, ?>> getDirectChildren() {
            List<SortableColumn<LeaderboardRowDAO, ?>> result = new ArrayList<SortableColumn<LeaderboardRowDAO, ?>>();
            for (SortableColumn<LeaderboardRowDAO, ?> column : super.getDirectChildren()) {
                result.add(column);
            }
            if (isExpanded()) {
                // it is important to re-use existing LegColumn objects because
                // removing the columns from the table
                // is based on column identity
                int legCount = getLeaderboard().getLegCount(getRaceName());
                if (legCount != -1) {
                    for (int i = 0; i < legCount; i++) {
                        LegColumn legColumn = getLegColumn(i);
                        result.add(legColumn);
                    }
                } else {
                    // the race is no longer part of the LeaderboardDAO; consider the non-null legs in legColumns:
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
                result = new LegColumn(LeaderboardPanel.this, getRaceName(), legNumber, stringConstants,
                        Collections.unmodifiableList(selectedLegDetails), LEG_COLUMN_HEADER_STYLE, LEG_COLUMN_STYLE,
                        LEG_DETAIL_COLUMN_HEADER_STYLE, LEG_DETAIL_COLUMN_STYLE);
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
            public Double get(LeaderboardRowDAO row) {
                Double result = null;
                LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    double distanceTraveledInMeters = 0;
                    long timeInMilliseconds = 0;
                    for (LegEntryDAO legDetail : fieldsForRace.legDetails) {
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
         * Accumulates the distance traveled over all legs of a race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceDistanceTraveledInMeters implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDAO row) {
                Double result = null;
                LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    for (LegEntryDAO legDetail : fieldsForRace.legDetails) {
                        if (legDetail != null) {
                            if (result == null) {
                                result = 0.0;
                            }
                            result += legDetail.distanceTraveledInMeters;
                        }
                    }
                }
                return result;
            }
        }

        /**
         * Accumulates the average speed over all legs of a race
         * 
         * @author Axel Uhl (D043530)
         */
        private class RaceGapToLeaderInSeconds implements LegDetailField<Double> {
            @Override
            public Double get(LeaderboardRowDAO row) {
                Double result = null;
                LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
                if (fieldsForRace != null && fieldsForRace.legDetails != null) {
                    LegEntryDAO lastLegDetail = fieldsForRace.legDetails.get(fieldsForRace.legDetails.size() - 1);
                    if (lastLegDetail != null) {
                        result = lastLegDetail.gapToLeaderInSeconds;
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
    private class TotalsColumn extends SortableColumn<LeaderboardRowDAO, String> {
        private final String columnStyle;

        protected TotalsColumn(String columnStyle) {
            super(new TextCell());
            this.columnStyle = columnStyle;
            setHorizontalAlignment(ALIGN_CENTER);
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            int totalPoints = getLeaderboard().getTotalPoints(object);
            return "" + totalPoints;
        }

        @Override
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<b>");
            sb.appendEscaped(getValue(object));
            sb.appendHtmlConstant("</b>");
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return getLeaderboard().getTotalRankingComparator();
        }

        @Override
        public String getColumnStyle() {
            return columnStyle;
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.total());
        }
    }

    protected class CarryColumn extends SortableColumn<LeaderboardRowDAO, String> {
        public CarryColumn() {
            super(new TextCell());
            setSortable(true);
        }

        protected CarryColumn(EditTextCell editTextCell) {
            super(editTextCell);
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return object.carriedPoints == null ? "" : "" + object.carriedPoints;
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return (o1.carriedPoints == null ? 0 : o1.carriedPoints)
                            - (o2.carriedPoints == null ? 0 : o2.carriedPoints);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.carry());
        }
    }

    private class RankColumn extends SortableColumn<LeaderboardRowDAO, String> {
        public RankColumn() {
            super(new TextCell());
            setHorizontalAlignment(ALIGN_CENTER);
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return "" + getLeaderboard().getRank(object.competitor);
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return getLeaderboard().getRank(o1.competitor) - getLeaderboard().getRank(o2.competitor);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.rank());
        }
    }

    public LeaderboardPanel(SailingServiceAsync sailingService, String leaderboardName, ErrorReporter errorReporter,
            final StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.setLeaderboardName(leaderboardName);
        this.errorReporter = errorReporter;
        this.stringConstants = stringConstants;
        this.selectedLegDetails = new ArrayList<DetailColumnType>();
        this.selectedLegDetails.add(DetailColumnType.DISTANCE_TRAVELED);
        this.selectedLegDetails.add(DetailColumnType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        this.selectedLegDetails.add(DetailColumnType.RANK_GAIN);
        this.selectedRaceDetails = new ArrayList<DetailColumnType>();
        this.selectedRaceColumns = new ArrayList<String>();
        this.firstTimeLeaderboardLoaded = true;
        delayInMilliseconds = 0l;
        timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */ 3000l);
        timer.setDelay(getDelayInMilliseconds()); // set time/delay before
                                                  // adding as listener
        timer.addPlayStateListener(this);
        timer.addTimeListener(this);
        rankColumn = new RankColumn();
        LeaderboardTableResources resources = GWT.create(LeaderboardTableResources.class);
        RACE_COLUMN_HEADER_STYLE = resources.cellTableStyle().cellTableRaceColumnHeader();
        LEG_COLUMN_HEADER_STYLE = resources.cellTableStyle().cellTableLegColumnHeader();
        LEG_DETAIL_COLUMN_HEADER_STYLE = resources.cellTableStyle().cellTableLegDetailColumnHeader();
        RACE_COLUMN_STYLE = resources.cellTableStyle().cellTableRaceColumn();
        LEG_COLUMN_STYLE = resources.cellTableStyle().cellTableLegColumn();
        LEG_DETAIL_COLUMN_STYLE = resources.cellTableStyle().cellTableLegDetailColumn();
        TOTAL_COLUMN_STYLE = resources.cellTableStyle().cellTableTotalColumn();
        leaderboardTable = new CellTableWithStylableHeaders<LeaderboardRowDAO>(
        /* pageSize */100, resources);
        getLeaderboardTable().setWidth("100%");
        getLeaderboardTable().setSelectionModel(new MultiSelectionModel<LeaderboardRowDAO>() {});
        setData(new ListDataProvider<LeaderboardRowDAO>());
        getData().addDataDisplay(getLeaderboardTable());
        listHandler = new ListHandler<LeaderboardRowDAO>(getData().getList());
        getLeaderboardTable().addColumnSortHandler(listHandler);
        loadCompleteLeaderboard(getLeaderboardDisplayDate());

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(15);
        DockPanel dockPanel = new DockPanel();
        DockPanel dockPanel02 = new DockPanel();
        dockPanel.setWidth("100%");
        dockPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        dockPanel02.setWidth("100%");
        dockPanel02.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Label leaderboardLabel = new Label(stringConstants.leaderboard() + " " + leaderboardName.toUpperCase());
        leaderboardLabel.addStyleName("leaderboardLabel boldLabel");
        dockPanel.add(leaderboardLabel, DockPanel.WEST);
        ClickHandler playPauseHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (timer.isPlaying()) {
                    timer.pause();
                } else {
                    timer.setDelay(getDelayInMilliseconds());
                    timer.resume();
                }
            }
        };
        HorizontalPanel refreshAndSettingsPanel = new HorizontalPanel();
        refreshAndSettingsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        HorizontalPanel refreshPanel = new HorizontalPanel();
        refreshPanel.setSpacing(5);
        refreshPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        refreshPanel.addStyleName("refreshPanel");
        dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        dockPanel02.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        dockPanel02.addStyleName("refreshAndSettings");
        playPause = new Anchor(getPlayPauseImgHtml(timer.isPlaying()));
        playPause.addClickHandler(playPauseHandler);
        playStateChanged(timer.isPlaying());
        refreshPanel.add(playPause);
        Anchor settingsAnchor = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/settings.png\"/>").toSafeHtml());
        settingsAnchor.setTitle(stringConstants.settings());
        settingsAnchor.addClickHandler(new SettingsClickHandler(stringConstants));
        refreshAndSettingsPanel.add(refreshPanel);
        refreshAndSettingsPanel.add(settingsAnchor);
        dockPanel02.add(refreshAndSettingsPanel, DockPanel.EAST);
        vp.add(dockPanel);
        vp.add(dockPanel02);
        vp.add(getLeaderboardTable());
        setWidget(vp);
    }


    private SafeHtml getPlayPauseImgHtml(boolean playing) {
        return new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/" + (playing ? "pause" : "play") + "_16.png\"/>")
                .toSafeHtml();
    }

    private long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }

    private void setDelayInMilliseconds(long delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    /**
     * The time point for which the leaderboard currently shows results
     */
    protected Date getLeaderboardDisplayDate() {
        return timer.getTime();
    }

    /**
     * adds the <code>column</code> to the right end of the {@link #getLeaderboardTable() leaderboard table} and sets
     * the column style according to the {@link SortableColumn#getColumnStyle() column's style definition}.
     */
    protected void addColumn(SortableColumn<LeaderboardRowDAO, ?> column) {
        getLeaderboardTable().addColumn(column, column.getHeader());
        listHandler.setComparator(column, column.getComparator());
        String columnStyle = column.getColumnStyle();
        if (columnStyle != null) {
            getLeaderboardTable().addColumnStyleName(getLeaderboardTable().getColumnCount() - 1, columnStyle);
        }
    }

    protected void insertColumn(int beforeIndex, SortableColumn<LeaderboardRowDAO, ?> column) {
        // remove column styles of those columns whose index will shift right by
        // one:
        removeColumnStyles(beforeIndex);
        getLeaderboardTable().insertColumn(beforeIndex, column, column.getHeader());
        addColumnStyles(beforeIndex);
        listHandler.setComparator(column, column.getComparator());
    }

    private void addColumnStyles(int startColumn) {
        for (int i = startColumn; i < getLeaderboardTable().getColumnCount(); i++) {
            SortableColumn<LeaderboardRowDAO, ?> columnToRemoveStyleFor = (SortableColumn<LeaderboardRowDAO, ?>) getLeaderboardTable()
                    .getColumn(i);
            String columnStyle = columnToRemoveStyleFor.getColumnStyle();
            if (columnStyle != null) {
                getLeaderboardTable().addColumnStyleName(i, columnStyle);
            }
        }
    }

    private void removeColumnStyles(int startColumn) {
        for (int i = startColumn; i < getLeaderboardTable().getColumnCount(); i++) {
            SortableColumn<LeaderboardRowDAO, ?> columnToRemoveStyleFor = (SortableColumn<LeaderboardRowDAO, ?>) getLeaderboardTable()
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
        Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(columnIndex);
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

    protected void removeColumn(Column<LeaderboardRowDAO, ?> c) {
        int columnIndex = getLeaderboardTable().getColumnIndex(c);
        if (columnIndex != -1) {
            removeColumn(columnIndex);
        }
    }

    private void loadCompleteLeaderboard(Date date) {
        getSailingService().getLeaderboardByName(getLeaderboardName(), date,
        /* namesOfRacesForWhichToLoadLegDetails */getNamesOfExpandedRaces(), new AsyncCallback<LeaderboardDAO>() {
            @Override
            public void onSuccess(LeaderboardDAO result) {
                updateLeaderboard(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                getErrorReporter().reportError("Error trying to obtain leaderboard contents: " + caught.getMessage());
            }
        });
    }

    /**
     * Determine from column expansion state which races need their leg details
     */
    private Collection<String> getNamesOfExpandedRaces() {
        Collection<String> namesOfExpandedRaces = new ArrayList<String>();
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDAO, ?> column = getLeaderboardTable().getColumn(i);
            if (column instanceof RaceColumn<?>) {
                RaceColumn<?> raceColumn = (RaceColumn<?>) column;
                if (raceColumn.isExpanded()) {
                    namesOfExpandedRaces.add(raceColumn.getRaceName());
                }
            }
        }
        return namesOfExpandedRaces;
    }

    /**
     * Also updates the min/max values on the columns
     */
    protected void updateLeaderboard(LeaderboardDAO leaderboard) {
        setLeaderboard(leaderboard);
        adjustColumnLayout(leaderboard);
        getData().getList().clear();
        if (leaderboard != null) {
            getData().getList().addAll(leaderboard.rows.values());
            for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
                SortableColumn<?, ?> c = (SortableColumn<?, ?>) getLeaderboardTable().getColumn(i);
                c.updateMinMax(leaderboard);
            }
            Comparator<LeaderboardRowDAO> comparator = getComparatorForSelectedSorting();
            if (comparator != null) {
                Collections.sort(getData().getList(), comparator);
            } else {
                // if no sorting was selected, sort by ascending rank and mark
                // table header so
                Collections.sort(getData().getList(), getRankColumn().getComparator());
                getLeaderboardTable().getColumnSortList().push(getRankColumn());
            }
        }
    }

    private Comparator<LeaderboardRowDAO> getComparatorForSelectedSorting() {
        Comparator<LeaderboardRowDAO> result = null;
        if (getLeaderboardTable().getColumnSortList().size() > 0) {
            ColumnSortInfo columnSortInfo = getLeaderboardTable().getColumnSortList().get(0);
            @SuppressWarnings("unchecked")
            SortableColumn<LeaderboardRowDAO, ?> castResult = (SortableColumn<LeaderboardRowDAO, ?>) columnSortInfo
                    .getColumn();
            if (columnSortInfo.isAscending()) {
                result = castResult.getComparator();
            } else {
                result = Collections.reverseOrder(castResult.getComparator());
            }
        }
        return result;
    }

    private RankColumn getRankColumn() {
        return rankColumn;
    }

    private void setLeaderboard(LeaderboardDAO leaderboard) {
        this.leaderboard = leaderboard;
    }

    protected LeaderboardDAO getLeaderboard() {
        return leaderboard;
    }

    private void adjustColumnLayout(LeaderboardDAO leaderboard) {
        ensureRankColumn();
        ensureSailIDAndCompetitorColumn();
        updateCarryColumn(leaderboard);
        // first remove race columns no longer needed:
        removeUnusedRaceColumns(leaderboard);
        if (leaderboard != null) {
            if(firstTimeLeaderboardLoaded){
                for (String string : leaderboard.getRaceList()) {
                    selectedRaceColumns.add(string);
                }
                if(selectedRaceColumns.size()!=0){
                    firstTimeLeaderboardLoaded = false;
                }
            }
            createMissingAndAdjustExistingRaceColumns(leaderboard);
            ensureTotalsColumn();
        }
    }

    private boolean leaderboardTableContainsRace(String raceName) {
        for (int leaderboardposition = 0; leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                RaceColumn<?> raceColumn = (RaceColumn<?>) c;
                if (raceColumn.getRaceName().equals(raceName)) {
                    correctColumnData(raceColumn);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Corrects the data linke medalRace of the given raceColumn
     * @param raceColumn the raceColumn to correct.
     */
    private void correctColumnData(RaceColumn<?> raceColumn){
        String race = raceColumn.getRaceName();
        int columnIndex = getRaceColumnPosition(raceColumn);
        if (raceColumn.isExpansionEnabled() != leaderboard.raceIsTracked(race)
                || leaderboard.raceIsMedalRace(race) != raceColumn.isMedalRace()) {
          if (raceColumn.isExpanded()) {
              raceColumn.toggleExpansion(); // remove children from table
          }
          removeColumn(columnIndex);
          insertColumn(
                  columnIndex,
                  createRaceColumn(race, leaderboard.raceIsMedalRace(race),
                          leaderboard.raceIsTracked(race)));
        }
    }

    /**
     * Removes all RaceColumns, starting at count {@link raceColumnStartIndex raceColumnStartIndex}
     * @param raceColumnStartIndex The index of the race column should be deleted from.
     * @param raceName The name of the racing column until the table should be cleared.
     */
    private void removeRaceColumnFromRaceColumnStartIndexBeforeRace(int raceColumnStartIndex, String raceName) {
        int counter = 0;
        for (int leaderboardposition = 0; leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                RaceColumn<?> raceColumn = (RaceColumn<?>) c;
                if (!raceColumn.getRaceName().equals(raceName) && raceColumnStartIndex==counter) {
                    removeColumn(raceColumn);
                } else {
                    return;
                }
                counter++;
            }
        }
    }
    
    /**
     * Gets a ColumnPosition of a raceColumn
     * @param raceColumn The column for which the position is to be found in the leaderboard table
     * @return the position. Returns -1 if raceColumn not existing in leaderboardTable.
     */
    private int getRaceColumnPosition(RaceColumn<?> raceColumn){
        for (int leaderboardposition = 0; leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                RaceColumn<?> rc = (RaceColumn<?>) c;
                if(rc.equals(raceColumn)){
                    return leaderboardposition;
                }
            }
        }
        return -1;
    }

    /**
     * This method returns the position where a racecolumn should get inserted.
     * @param raceName the name of the race to insert
     * @param the position of the race in the  {@link selectedRaceColumns selectedRaceColumns}
     * @return the position to insert the racecolumn
     */
    private int getColumnPositionToInsert(String raceName, int listpos) {
        int raceColumnCounter = 0;
        int noRaceColumnCounter = 0;
        boolean raceColumnfound = false;
        for (int leaderboardposition = 0; !raceColumnfound & leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                //RaceColumn<?> raceColumn = (RaceColumn<?>) c;
                if (raceColumnCounter == listpos) {
                    raceColumnfound = true;
                }
                raceColumnCounter++;
            } else {
                noRaceColumnCounter++;
            }
        }
        if(raceColumnfound){
            return raceColumnCounter + noRaceColumnCounter;
        }else{
            return -1;
        }
    }
    
    /**
     * Removes all Columns of type racecolumns of leaderboardTable
     */
    private void removeRaceColumnNotUsed() {
        for (int leaderboardposition = 0; leaderboardposition < getLeaderboardTable().getColumnCount(); leaderboardposition++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(leaderboardposition);
            if (c instanceof RaceColumn) {
                RaceColumn<?> raceColumn = (RaceColumn<?>) c;
                if(!selectedRaceColumns.contains(raceColumn.getRaceName())){
                    if(raceColumn.isExpanded()){
                        raceColumn.toggleExpansion();
                    }
                    getLeaderboardTable().removeColumn(raceColumn);
                }
            }
        }
    }
    
    /**
     * Existing and matching race columns may still need to be removed, re-created and inserted because the "tracked"
     * property may have changed, changing the columns expandability.
     */
    private void createMissingAndAdjustExistingRaceColumns(LeaderboardDAO leaderboard) {
        // Correct order of races in selectedRaceColum
        List<String> correctedOrderSelectedRaces = new ArrayList<String>();
        for (String string : leaderboard.getRaceList()) {
            if (selectedRaceColumns.contains(string)) {
                correctedOrderSelectedRaces.add(string);
            }
        }
        selectedRaceColumns = correctedOrderSelectedRaces;
        
        removeRaceColumnNotUsed();
        
        for (int selectedRaceCount = 0; selectedRaceCount < selectedRaceColumns.size(); selectedRaceCount++) {
            String selectedRaceName = selectedRaceColumns.get(selectedRaceCount);
            if (leaderboardTableContainsRace(selectedRaceName)){
                // remove all raceColumns, starting at a specific raceColumnPosition, until the selected raceName.
                removeRaceColumnFromRaceColumnStartIndexBeforeRace(selectedRaceCount, selectedRaceName);
            }else{
                // get correct position to insert the column
                int positionToInsert = getColumnPositionToInsert(selectedRaceName, selectedRaceCount);
                if(positionToInsert!=-1){
                    insertColumn(positionToInsert,
                            createRaceColumn(selectedRaceName, leaderboard.raceIsMedalRace(selectedRaceName),
                                    leaderboard.raceIsTracked(selectedRaceName)));
                }else{
                    // Add the raceColumn with addRaceColumn, if no RaceColumn is existing in leaderboard
                    addRaceColumn(createRaceColumn(selectedRaceName, leaderboard.raceIsMedalRace(selectedRaceName),
                                    leaderboard.raceIsTracked(selectedRaceName)));
                }
            }
        }
    }

    protected RaceColumn<?> createRaceColumn(String raceName, boolean isMedalRace, boolean isTracked) {
        TextRaceColumn textRaceColumn = new TextRaceColumn(raceName, isMedalRace, isTracked, RACE_COLUMN_HEADER_STYLE,
                RACE_COLUMN_STYLE);
        return textRaceColumn;
    }

    private void removeUnusedRaceColumns(LeaderboardDAO leaderboard) {
        List<Column<LeaderboardRowDAO, ?>> columnsToRemove = new ArrayList<Column<LeaderboardRowDAO, ?>>();
        for (int i = 0; i < getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(i);
            if (c instanceof RaceColumn
                    && (leaderboard == null || !leaderboard.raceListContains(((RaceColumn<?>) c).getRaceName()))) {
                columnsToRemove.add(c);
            }
        }
        // Tricky issue: if the race column is currently expanded, we can't know anymore how many detail columns
        // there are because the updated LeaderboardDAO object doesn't contain the race anymore. We have to
        // collapse and remove all LegColumns following the RaceColumn
        for (Column<LeaderboardRowDAO, ?> c : columnsToRemove) {
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
            addColumn(getRankColumn());
        } else {
            if (!(getLeaderboardTable().getColumn(RANK_COLUMN_INDEX) instanceof RankColumn)) {
                throw new RuntimeException("The first column must always be the rank column but it was of type "
                        + getLeaderboardTable().getColumn(RANK_COLUMN_INDEX).getClass().getName());
            }
        }
    }

    private void ensureSailIDAndCompetitorColumn() {
        if (getLeaderboardTable().getColumnCount() <= SAIL_ID_COLUMN_INDEX) {
            addColumn(new SailIDColumn());
            addColumn(createCompetitorColumn());
        } else {
            if (!(getLeaderboardTable().getColumn(SAIL_ID_COLUMN_INDEX) instanceof SailIDColumn)) {
                throw new RuntimeException("The second column must always be the sail ID column but it was of type "
                        + getLeaderboardTable().getColumn(SAIL_ID_COLUMN_INDEX).getClass().getName());
            }
        }
    }

    protected CompetitorColumn createCompetitorColumn() {
        return new CompetitorColumn();
    }

    private void ensureTotalsColumn() {
        // add a totals column on the right
        if (getLeaderboardTable().getColumnCount() == 0
                || !(getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount() - 1) instanceof TotalsColumn)) {
            addColumn(new TotalsColumn(TOTAL_COLUMN_STYLE));
        }
    }

    /**
     * If the <code>leaderboard</code> {@link LeaderboardDAO#hasCarriedPoints has carried points} and if column #1
     * (second column, right of the competitor column) does not exist or is not of type {@link CarryColumn}, all columns
     * starting from #1 will be removed and a {@link CarryColumn} will be added. If the leaderboard has no carried
     * points but the display still shows a carry column, the column is removed.
     */
    protected void updateCarryColumn(LeaderboardDAO leaderboard) {
        if (leaderboard != null && leaderboard.hasCarriedPoints) {
            ensureCarryColumn();
        } else {
            ensureNoCarryColumn();
        }
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

    protected CellTable<LeaderboardRowDAO> getLeaderboardTable() {
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

    protected ListDataProvider<LeaderboardRowDAO> getData() {
        return data;
    }

    private void setData(ListDataProvider<LeaderboardRowDAO> data) {
        this.data = data;
    }

    @Override
    public void timeChanged(Date date) {
        loadCompleteLeaderboard(getLeaderboardDisplayDate());
    }

    @Override
    public void playStateChanged(boolean isPlaying) {
        playPause.setHTML(getPlayPauseImgHtml(isPlaying));
        playPause.setTitle(isPlaying ? stringConstants.pauseAutomaticRefresh() : stringConstants.autoRefresh());
    }
}
