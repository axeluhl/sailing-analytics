package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

/**
 * An editable version of the {@link LeaderboardPanel} which allows a user to enter carried / accumulated
 * points and fix individual race scores.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class EditableLeaderboardPanel extends LeaderboardPanel {
    private class EditableCarryColumn extends CarryColumn {
        public EditableCarryColumn() {
            super(new EditTextCell());
            setFieldUpdater(new FieldUpdater<LeaderboardRowDAO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDAO row, final String value) {
                    getSailingService().updateLeaderboardCarryValue(getLeaderboardName(), row.competitor.name,
                            value == null || value.length() == 0 ? null : Integer.valueOf(value),
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    EditableLeaderboardPanel.this.getErrorReporter().reportError("Error trying to update carry value for competitor "+
                                            row.competitor.name+" in leaderboard "+getLeaderboardName()+": "+t.getMessage()+
                                            "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Void v) {
                                    row.carriedPoints = value==null||value.length()==0 ? null : Integer.valueOf(value);
                                    List<LeaderboardRowDAO> list = EditableLeaderboardPanel.this.getData().getList();
                                    getLeaderboardTable().setRowData(rowIndex, list.subList(rowIndex, list.size()));
                                }
                            });
                }
            });
        }
    }

    private class EditableRaceColumn extends RaceColumn<LeaderboardRowDAO> {
        public EditableRaceColumn(String raceName, boolean medalRace, List<HasCell<LeaderboardRowDAO, ?>> cellList) {
            super(raceName, medalRace, new CompositeCell<LeaderboardRowDAO>(cellList));
            // no field updater for the composite; individual fields are updated individually
        }

        @Override
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder html) {
            defaultRender(context, object, html);
        }

        @Override
        public LeaderboardRowDAO getValue(LeaderboardRowDAO object) {
            return object;
        }
    }
    
    private class MaxPointsDropDownCellProvider implements HasCell<LeaderboardRowDAO, String> {
        private final Cell<String> dropDownCell;
        private final String raceName;
        
        public MaxPointsDropDownCellProvider(String raceName) {
            this.raceName = raceName;
            dropDownCell = new SelectionCell(Arrays.asList(new String[] { "", "DNS", "DNF", "OCS", "DND", "RAF", "BFD", "DNC", "DSQ" }));
        }
        
        @Override
        public Cell<String> getCell() {
            return dropDownCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDAO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDAO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDAO row, final String value) {
                  getSailingService().updateLeaderboardMaxPointsReason(getLeaderboardName(), row.competitor.name, raceName,
                          value == null || value.trim().length() == 0 ? null : value.trim(),
                  new AsyncCallback<Void>() {
                      @Override
                      public void onFailure(Throwable t) {
                          getErrorReporter().reportError("Error trying to update max points reason for competitor "+
                                  row.competitor.name+" in leaderboard "+getLeaderboardName()+": "+t.getMessage()+
                                  "\nYou may have to refresh your view.");
                      }

                      @Override
                      public void onSuccess(Void v) {
                          row.fieldsByRaceName.get(raceName).reasonForMaxPoints = value==null||value.length()==0 ? null : value.trim();
                          List<LeaderboardRowDAO> list = EditableLeaderboardPanel.this.getData().getList();
                          getLeaderboardTable().setRowData(rowIndex, list.subList(rowIndex, list.size()));
                      }
                  });
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            String reasonForMaxPoints = object.fieldsByRaceName.get(raceName).reasonForMaxPoints;
            return reasonForMaxPoints == null ? "" : reasonForMaxPoints;
        }
    }
    
    private class NetPointsEditCellProvider implements HasCell<LeaderboardRowDAO, String> {
        private final Cell<String> netPointsEditCell;
        private final String raceName;
        
        protected NetPointsEditCellProvider(String raceName) {
            this.raceName = raceName;
            netPointsEditCell = new EditTextCell();
        }
        
        @Override
        public Cell<String> getCell() {
            return netPointsEditCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDAO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDAO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDAO row, final String value) {
                    getSailingService().updateLeaderboardScoreCorrection(getLeaderboardName(), row.competitor.name, raceName,
                            value == null || value.trim().length() == 0 ? null : Integer.valueOf(value.trim()), new Date(),
                    new AsyncCallback<Integer>() {
                        @Override
                        public void onFailure(Throwable t) {
                            getErrorReporter().reportError("Error trying to update score correction for competitor "+
                                    row.competitor.name+" in leaderboard "+getLeaderboardName()+
                                    " for race "+raceName+": "+t.getMessage()+
                                    "\nYou may have to refresh your view.");
                        }

                        @Override
                        public void onSuccess(Integer newNetPoints) {
                            row.fieldsByRaceName.get(raceName).netPoints = value==null||value.length()==0 ? newNetPoints : Integer.valueOf(value.trim());
                            List<LeaderboardRowDAO> list = EditableLeaderboardPanel.this.getData().getList();
                            getLeaderboardTable().setRowData(rowIndex, list.subList(rowIndex, list.size()));
                        }
                    });
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            return ""+object.fieldsByRaceName.get(raceName).netPoints;
        }
    }

    public EditableLeaderboardPanel(SailingServiceAsync sailingService, String leaderboardName,
            ErrorReporter errorReporter, StringConstants stringConstants) {
        super(sailingService, leaderboardName, errorReporter, stringConstants);
    }

    /**
     * Always ensures that there is a carry column displayed because in the editable version
     * of the leaderboard the carried / accumulated values must always be editable and therefore
     * the column must always be shown.
     */
    @Override
    protected void updateCarryColumn(LeaderboardDAO leaderboard) {
        ensureCarryColumn();
    }

    @Override
    protected CarryColumn createCarryColumn() {
        return new EditableCarryColumn();
    }

    @Override
    protected RaceColumn<?> createRaceColumn(Entry<String, Boolean> raceNameAndMedalRace) {
        return new EditableRaceColumn(raceNameAndMedalRace.getKey(), raceNameAndMedalRace.getValue(),
                getCellList(raceNameAndMedalRace.getKey(), raceNameAndMedalRace.getValue()));
    }

    private List<HasCell<LeaderboardRowDAO, ?>> getCellList(String raceName, boolean medalRace) {
        List<HasCell<LeaderboardRowDAO, ?>> list = new ArrayList<HasCell<LeaderboardRowDAO, ?>>();
        list.add(new MaxPointsDropDownCellProvider(raceName));
        list.add(new NetPointsEditCellProvider(raceName));
        return list;
    }

}
