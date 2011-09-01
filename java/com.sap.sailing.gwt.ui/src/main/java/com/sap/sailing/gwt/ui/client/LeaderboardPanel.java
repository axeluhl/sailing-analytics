package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

/**
 * A leaderboard essentially consists of a table widget that in its columns displays the entries.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LeaderboardPanel extends FormPanel {
    private final SailingServiceAsync sailingService;
    
    /**
     * The leaderboard name is used to
     * {@link SailingServiceAsync#getLeaderboardByName(String, java.util.Date, com.google.gwt.user.client.rpc.AsyncCallback)
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

    private class CompetitorColumn extends SortableColumn<LeaderboardRowDAO, String> {

        protected CompetitorColumn() {
            super(new TextCell());
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return Collator.getInstance().compare(o1.competitor.name, o2.competitor.name);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.competitor());
        }

        @Override
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<img title=\""+object.competitor.countryName+"\" src=\""+
                    getFlagURL(object.competitor.twoLetterIsoCountryCode)+
                    "\"/>&nbsp;");
            sb.appendHtmlConstant(object.competitor.name);
        }

        private String getFlagURL(String twoLetterIsoCountryCode) {
            return "/images/flags/"+twoLetterIsoCountryCode.toLowerCase()+".png";
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return object.competitor.name;
        }
        
    }
    
    /**
     * Displays net/total points and possible max-points reasons based on a {@link LeaderboardRowDAO} and a
     * race name and makes the column sortable by the total points.
     *  
     * @author Axel Uhl (D043530)
     *
     */
    protected abstract class RaceColumn<C> extends SortableColumn<LeaderboardRowDAO, C> {
        private final String raceName;
        private final boolean medalRace;

        public RaceColumn(String raceName, boolean medalRace, Cell<C> cell) {
            super(cell);
            this.raceName = raceName;
            this.medalRace = medalRace;
        }
        
        public String getRaceName() {
            return raceName;
        }
        
        protected void defaultRender(Context context, LeaderboardRowDAO object, SafeHtmlBuilder html) {
            super.render(context, object, html);
        }
        
        @Override
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder html) {
            LeaderboardEntryDAO entry = object.fieldsByRaceName.get(raceName);
            if (entry != null) {
                if (entry.discarded) {
                    html.appendHtmlConstant("<del>");
                }
                html.append(entry.totalPoints);
                if (entry.netPoints != entry.totalPoints) {
                    html.appendHtmlConstant(" (" + entry.netPoints + ")");
                }
                if (!entry.reasonForMaxPoints.equals("NONE")) {
                    html.appendEscapedLines("\n(" + entry.reasonForMaxPoints + ")");
                }
                if (entry.discarded) {
                    html.appendHtmlConstant("</del>");
                }
            }
        }
        
        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return o1.fieldsByRaceName.get(raceName).netPoints - o2.fieldsByRaceName.get(raceName).netPoints;
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(raceName) {
                @Override
                public void render(Context context, SafeHtmlBuilder sb) {
                    if (medalRace) {
                        sb.appendHtmlConstant("<img src=\"/images/medal.png\">");
                    }
                    super.render(context, sb);
                }
            };
        }
    }
    
    private class TextRaceColumn extends RaceColumn<String> {
        public TextRaceColumn(String raceName, boolean medalRace) {
            super(raceName, medalRace, new TextCell());
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return ""+object.fieldsByRaceName.get(getRaceName()).totalPoints;
        }
    }
    
    /**
     * Displays the totals for a competitor for the entire leaderboard.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class TotalsColumn extends SortableColumn<LeaderboardRowDAO, String>  {
        protected TotalsColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            int totalPoints = getLeaderboard().getTotalPoints(object);
            return ""+totalPoints;
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return getLeaderboard().getTotalRankingComparator();
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.total());
        }
    }
    
    protected class CarryColumn extends SortableColumn<LeaderboardRowDAO, String>  {
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
            return object.carriedPoints==null?"":""+object.carriedPoints;
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return (o1.carriedPoints==null?0:o1.carriedPoints) -
                           (o2.carriedPoints==null?0:o2.carriedPoints);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.carry());
        }
    }
    
    private class RankColumn extends SortableColumn<LeaderboardRowDAO, String>  {
        public RankColumn() {
            super(new TextCell());
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return ""+getLeaderboard().getRank(object.competitor);
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
            StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.setLeaderboardName(leaderboardName);
        this.errorReporter = errorReporter;
        this.stringConstants = stringConstants;
        rankColumn = new RankColumn();
        leaderboardTable = new CellTable<LeaderboardRowDAO>(/* pageSize */ 100);
        getLeaderboardTable().setWidth("100%");
        getLeaderboardTable().setSelectionModel(new MultiSelectionModel<LeaderboardRowDAO>() {});
        setData(new ListDataProvider<LeaderboardRowDAO>());
        getData().addDataDisplay(getLeaderboardTable());
        listHandler = new ListHandler<LeaderboardRowDAO>(getData().getList());
        getLeaderboardTable().addColumnSortHandler(listHandler);
        loadCompleteLeaderboard(new Date());
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(10);
        hp.add(new Label(leaderboardName));
        Button refreshButton = new Button(stringConstants.refresh());
        hp.add(refreshButton);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loadCompleteLeaderboard(new Date());
            }
        });
        vp.add(hp);
        vp.add(getLeaderboardTable());
        setWidget(vp);
    }
    
    /**
     * The time point for which the leaderboard currently shows results
     */
    protected Date getLeaderboardDisplayDate() {
        // TODO add a notion of selectable time to the leaderboard panel
        return new Date();
    }
    
    protected void addColumn(SortableColumn<LeaderboardRowDAO, ?> column) {
        getLeaderboardTable().addColumn(column, column.getHeader());
        listHandler.setComparator(column, column.getComparator());
    }
    
    private void loadCompleteLeaderboard(Date date) {
        getSailingService().getLeaderboardByName(getLeaderboardName(), date, new AsyncCallback<LeaderboardDAO>() {
            @Override
            public void onSuccess(LeaderboardDAO result) {
                updateLeaderboard(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                getErrorReporter().reportError("Error trying to obtain leaderboard contents: "+caught.getMessage());
            }
        });
    }
    
    private void updateLeaderboard(LeaderboardDAO leaderboard) {
        setLeaderboard(leaderboard);
        adjustColumnLayout(leaderboard);
        getData().getList().clear();
        if (leaderboard != null) {
            getData().getList().addAll(leaderboard.rows.values());
            Comparator<LeaderboardRowDAO> comparator = getComparatorForSelectedSorting();
            if (comparator != null) {
                Collections.sort(getData().getList(), comparator);
            } else {
                // if no sorting was selected, sort by ascending rank and mark table header so
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
            SortableColumn<LeaderboardRowDAO, ?> castResult = (SortableColumn<LeaderboardRowDAO, ?>) columnSortInfo.getColumn();
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
        ensureCompetitorColumn();
        updateCarryColumn(leaderboard);
        // first remove race columns no longer needed:
        removeUnusedRaceColumns(leaderboard);
        if (leaderboard != null) {
            createMissingRaceColumns(leaderboard);
            ensureTotalsColumn();
        }
    }

    private void createMissingRaceColumns(LeaderboardDAO leaderboard) {
        for (Map.Entry<String, Boolean> raceNameAndMedalRace : leaderboard.raceNamesAndMedalRace.entrySet()) {
            boolean foundRaceColumn = false;
            for (int i=0; !foundRaceColumn && i<getLeaderboardTable().getColumnCount(); i++) {
                Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(i);
                if (c instanceof RaceColumn && ((RaceColumn<?>) c).getRaceName().equals(raceNameAndMedalRace.getKey())) {
                    foundRaceColumn = true;
                }
            }
            if (!foundRaceColumn) {
                addRaceColumn(createRaceColumn(raceNameAndMedalRace));
            }
        }
    }

    protected RaceColumn<?> createRaceColumn(Map.Entry<String, Boolean> raceNameAndMedalRace) {
        return new TextRaceColumn(raceNameAndMedalRace.getKey(), raceNameAndMedalRace.getValue());
    }

    private void removeUnusedRaceColumns(LeaderboardDAO leaderboard) {
        List<Column<LeaderboardRowDAO, ?>> columnsToRemove = new ArrayList<Column<LeaderboardRowDAO,?>>();
        for (int i=0; i<getLeaderboardTable().getColumnCount(); i++) {
            Column<LeaderboardRowDAO, ?> c = getLeaderboardTable().getColumn(i);
            if (c instanceof RaceColumn
                    && (leaderboard == null || !leaderboard.raceNamesAndMedalRace.keySet().contains(
                            ((RaceColumn<?>) c).getRaceName()))) {
                columnsToRemove.add(c);
            }
        }
        for (Column<LeaderboardRowDAO, ?> c : columnsToRemove) {
            getLeaderboardTable().removeColumn(c);
        }
    }

    /**
     * If the last column is the totals column, remove it. Add the race column as the last column.
     */
    private void addRaceColumn(RaceColumn<?> raceColumn) {
        if (getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount()-1) instanceof TotalsColumn) {
            getLeaderboardTable().removeColumn(getLeaderboardTable().getColumnCount()-1);
        }
        addColumn(raceColumn);
    }

    private void ensureRankColumn() {
        if (getLeaderboardTable().getColumnCount() == 0) {
            addColumn(getRankColumn());
        } else {
            if (!(getLeaderboardTable().getColumn(0) instanceof RankColumn)) {
                throw new RuntimeException("The first column must always be the rank column but it was of type "+
                        getLeaderboardTable().getColumn(0).getClass().getName());
            }
        }
    }

    private void ensureCompetitorColumn() {
        if (getLeaderboardTable().getColumnCount() < 2) {
            addColumn(new CompetitorColumn());
        } else {
            if (!(getLeaderboardTable().getColumn(1) instanceof CompetitorColumn)) {
                throw new RuntimeException("The second column must always be the competitor column but it was of type "+
                        getLeaderboardTable().getColumn(1).getClass().getName());
            }
        }
    }

    private void ensureTotalsColumn() {
        // add a totals column on the right
        if (getLeaderboardTable().getColumnCount() == 0 || !(getLeaderboardTable().getColumn(getLeaderboardTable().getColumnCount()-1) instanceof TotalsColumn)) {
            addColumn(new TotalsColumn());
        }
    }

    /**
     * If the <code>leaderboard</code> {@link LeaderboardDAO#hasCarriedPoints has carried points} and if column #1
     * (second column, right of the competitor column) does not exist or is not of type {@link CarryColumn}, all columns
     * starting from #1 will be removed and a {@link CarryColumn} will be added. If the leaderboard has no carried points
     * but the display still shows a carry column, the column is removed.
     */
    protected void updateCarryColumn(LeaderboardDAO leaderboard) {
        if (leaderboard != null && leaderboard.hasCarriedPoints) {
            ensureCarryColumn();
        } else {
            ensureNoCarryColumn();
        }
    }

    private void ensureNoCarryColumn() {
        if (getLeaderboardTable().getColumnCount() >= 3 && getLeaderboardTable().getColumn(2) instanceof CarryColumn) {
            getLeaderboardTable().removeColumn(2);
        }
    }

    protected void ensureCarryColumn() {
        if (getLeaderboardTable().getColumnCount() < 3 || !(getLeaderboardTable().getColumn(2) instanceof CarryColumn)) {
            while (getLeaderboardTable().getColumnCount() > 2) {
                getLeaderboardTable().removeColumn(2);
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

}
