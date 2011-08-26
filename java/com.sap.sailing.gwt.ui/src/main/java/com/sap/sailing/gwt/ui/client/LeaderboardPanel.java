package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
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

    private CellTable<LeaderboardRowDAO> leaderboardTable;

    private ListDataProvider<LeaderboardRowDAO> data;
    
    private final ListHandler<LeaderboardRowDAO> listHandler;

    private class CompetitorColumn extends SortableColumn<LeaderboardRowDAO> {

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return o1.competitor.name.compareTo(o2.competitor.name);
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
    private class RaceColumn extends SortableColumn<LeaderboardRowDAO> {
        private final String raceName;
        private final boolean medalRace;

        public RaceColumn(String raceName, boolean medalRace) {
            this.raceName = raceName;
            this.medalRace = medalRace;
        }
        
        public String getRaceName() {
            return raceName;
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
        public String getValue(LeaderboardRowDAO object) {
            return ""+object.fieldsByRaceName.get(raceName).totalPoints;
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
    
    /**
     * Displays the totals for a competitor for the entire leaderboard.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class TotalsColumn extends SortableColumn<LeaderboardRowDAO>  {
        @Override
        public String getValue(LeaderboardRowDAO object) {
            int totalPoints = getTotalPoints(object);
            return ""+totalPoints;
        }

        private int getTotalPoints(LeaderboardRowDAO object) {
            int totalPoints = object.carriedPoints;
            for (LeaderboardEntryDAO e : object.fieldsByRaceName.values()) {
                totalPoints += e.totalPoints;
            }
            return totalPoints;
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return getTotalPoints(o1) - getTotalPoints(o2);
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.total());
        }
    }
    
    private class CarryColumn extends SortableColumn<LeaderboardRowDAO>  {
        public CarryColumn() {
            setSortable(true);
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return ""+object.carriedPoints;
        }

        @Override
        public Comparator<LeaderboardRowDAO> getComparator() {
            return new Comparator<LeaderboardRowDAO>() {
                @Override
                public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                    return o1.carriedPoints - o2.carriedPoints;
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringConstants.carry());
        }
    }
    
    public LeaderboardPanel(SailingServiceAsync sailingService, String leaderboardName, ErrorReporter errorReporter,
            StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.errorReporter = errorReporter;
        this.stringConstants = stringConstants;
        leaderboardTable = new CellTable<LeaderboardRowDAO>(/* pageSize */ 100);
        leaderboardTable.setWidth("100%");
        leaderboardTable.setSelectionModel(new MultiSelectionModel<LeaderboardRowDAO>() {});
        data = new ListDataProvider<LeaderboardRowDAO>();
        data.addDataDisplay(leaderboardTable);
        listHandler = new ListHandler<LeaderboardRowDAO>(data.getList());
        leaderboardTable.addColumnSortHandler(listHandler);
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
        vp.add(leaderboardTable);
        setWidget(vp);
    }
    
    private void addColumn(SortableColumn<LeaderboardRowDAO> column) {
        leaderboardTable.addColumn(column, column.getHeader());
        listHandler.setComparator(column, column.getComparator());
    }
    
    private void loadCompleteLeaderboard(Date date) {
        sailingService.getLeaderboardByName(leaderboardName, date, new AsyncCallback<LeaderboardDAO>() {
            @Override
            public void onSuccess(LeaderboardDAO result) {
                updateLeaderboard(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to obtain leaderboard contents: "+caught.getMessage());
            }
        });
    }
    
    private void updateLeaderboard(LeaderboardDAO leaderboard) {
        adjustColumnLayout(leaderboard);
        data.getList().clear();
        if (leaderboard != null) {
            data.getList().addAll(leaderboard.rows.values());
        }
    }
    
    private void adjustColumnLayout(LeaderboardDAO leaderboard) {
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
            for (int i=0; !foundRaceColumn && i<leaderboardTable.getColumnCount(); i++) {
                Column<LeaderboardRowDAO, ?> c = leaderboardTable.getColumn(i);
                if (c instanceof RaceColumn && ((RaceColumn) c).getRaceName().equals(raceNameAndMedalRace.getKey())) {
                    foundRaceColumn = true;
                }
            }
            if (!foundRaceColumn) {
                addRaceColumn(new RaceColumn(raceNameAndMedalRace.getKey(), raceNameAndMedalRace.getValue()));
            }
        }
    }

    private void removeUnusedRaceColumns(LeaderboardDAO leaderboard) {
        List<Column<LeaderboardRowDAO, ?>> columnsToRemove = new ArrayList<Column<LeaderboardRowDAO,?>>();
        for (int i=0; i<leaderboardTable.getColumnCount(); i++) {
            Column<LeaderboardRowDAO, ?> c = leaderboardTable.getColumn(i);
            if (c instanceof RaceColumn && (leaderboard == null || !leaderboard.raceNamesAndMedalRace.keySet().contains(((RaceColumn) c).getRaceName()))) {
                columnsToRemove.add(c);
            }
        }
        for (Column<LeaderboardRowDAO, ?> c : columnsToRemove) {
            leaderboardTable.removeColumn(c);
        }
    }

    /**
     * If the last column is the totals column, remove it. Add the race column as the last column.
     */
    private void addRaceColumn(RaceColumn raceColumn) {
        if (leaderboardTable.getColumn(leaderboardTable.getColumnCount()-1) instanceof TotalsColumn) {
            leaderboardTable.removeColumn(leaderboardTable.getColumnCount()-1);
        }
        addColumn(raceColumn);
    }

    private void ensureCompetitorColumn() {
        if (leaderboardTable.getColumnCount() == 0) {
            addColumn(new CompetitorColumn());
        } else {
            if (!(leaderboardTable.getColumn(0) instanceof CompetitorColumn)) {
                throw new RuntimeException("The first column must always be the competitor column but it was of type "+
                        leaderboardTable.getColumn(0).getClass().getName());
            }
        }
    }

    private void ensureTotalsColumn() {
        // add a totals column on the right
        if (leaderboardTable.getColumnCount() == 0 || !(leaderboardTable.getColumn(leaderboardTable.getColumnCount()-1) instanceof TotalsColumn)) {
            addColumn(new TotalsColumn());
        }
    }

    /**
     * If column #1 (second column, right of the competitor column) does not exist or is not of type {@link CarryColumn},
     * all columns starting from #1 will be removed and a {@link CarryColumn} will be added.
     */
    private void updateCarryColumn(LeaderboardDAO leaderboard) {
        if (leaderboard != null && leaderboard.hasCarriedPoints) {
            if (!(leaderboardTable.getColumn(1) instanceof CarryColumn)) {
                while (leaderboardTable.getColumnCount() > 1) {
                    leaderboardTable.removeColumn(1);
                }
                addColumn(new CarryColumn());
            }
        } else {
            if (leaderboardTable.getColumnCount() >= 2 && leaderboardTable.getColumn(1) instanceof CarryColumn) {
                leaderboardTable.removeColumn(1);
            }
        }
    }

}
