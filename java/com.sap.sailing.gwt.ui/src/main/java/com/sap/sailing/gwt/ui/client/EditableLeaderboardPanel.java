package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
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
                            value == null || value.length() == 0 ? null : Integer.valueOf(value.trim()),
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    EditableLeaderboardPanel.this.getErrorReporter().reportError("Error trying to update carry value for competitor "+
                                            row.competitor.name+" in leaderboard "+getLeaderboardName()+": "+t.getMessage()+
                                            "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Void v) {
                                    row.carriedPoints = value==null||value.length()==0 ? null : Integer.valueOf(value.trim());
                                    EditableLeaderboardPanel.this.getData().getList().set(rowIndex, row);
                                }
                            });
                }
            });
        }
    }
    
    private class EditableCompetitorColumn extends CompetitorColumn {
        
        @Override
        public EditTextCell getCell() {
            return (EditTextCell) super.getCell();
        }

        public EditableCompetitorColumn() {
            super(new EditTextCell());
            setFieldUpdater(new FieldUpdater<LeaderboardRowDAO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDAO row, final String value) {
                    getSailingService().updateCompetitorDisplayNameInLeaderboard(getLeaderboardName(), row.competitor.name,
                            value == null || value.length() == 0 ? null : value.trim(),
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    EditableLeaderboardPanel.this.getErrorReporter().reportError("Error trying to update display name for competitor "+
                                            row.competitor.name+" in leaderboard "+getLeaderboardName()+": "+t.getMessage()+
                                            "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Void v) {
                                    if (getLeaderboard().competitorDisplayNames == null) {
                                        getLeaderboard().competitorDisplayNames = new HashMap<CompetitorDAO, String>();
                                    }
                                    getCell().setViewData(row, null); // ensure that getValue() is called again
                                    getLeaderboard().competitorDisplayNames.put(row.competitor, value == null || value.trim().length() == 0 ? null : value.trim());
                                    EditableLeaderboardPanel.this.getData().getList().set(rowIndex, row);
                                }
                            });
                }
            });
        }
    }

    private class EditableRaceColumn extends RaceColumn<LeaderboardRowDAO> implements RowUpdateWhiteboardOwner<LeaderboardRowDAO> {
        private RowUpdateWhiteboard<LeaderboardRowDAO> currentRowUpdateWhiteboard;
        
        public EditableRaceColumn(String raceName, boolean medalRace, List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDAO, ?>> cellList) {
            super(raceName, medalRace,
                    /* expandable */ false, // we don't want leg expansion when editing scores
                    new CompositeCell<LeaderboardRowDAO>(new ArrayList<HasCell<LeaderboardRowDAO, ?>>(cellList)),
                    RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE);
            for (RowUpdateWhiteboardProducer<LeaderboardRowDAO> rowUpdateWhiteboardProducer : cellList) {
                rowUpdateWhiteboardProducer.setWhiteboardOwner(this);
            }
            // the field updater for the composite is invoked after any component has updated its field
            setFieldUpdater(new FieldUpdater<LeaderboardRowDAO, LeaderboardRowDAO>() {
                @Override
                public void update(int rowIndex, LeaderboardRowDAO row, LeaderboardRowDAO value) {
                    currentRowUpdateWhiteboard.setIndexOfRowToUpdate(rowIndex);
                    currentRowUpdateWhiteboard = null; // show that it has been consumed and updated
                }
            });
        }

        @Override
        public void render(Context context, LeaderboardRowDAO object, SafeHtmlBuilder html) {
            defaultRender(context, object, html);
        }

        @Override
        public LeaderboardRowDAO getValue(LeaderboardRowDAO object) {
            return object;
        }

        @Override
        public void whiteboardProduced(RowUpdateWhiteboard<LeaderboardRowDAO> whiteboard) {
            currentRowUpdateWhiteboard = whiteboard;
        }
    }
    
    private class MaxPointsDropDownCellProvider extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDAO, String> {
        private final SelectionCell dropDownCell;
        private final String raceName;
        
        public MaxPointsDropDownCellProvider(String raceName) {
            this.raceName = raceName;
            dropDownCell = new SelectionCell(Arrays.asList(new String[] { "", "DNS", "DNF", "OCS", "DND", "RAF", "BFD", "DNC", "DSQ" }));
        }
        
        @Override
        public SelectionCell getCell() {
            return dropDownCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDAO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDAO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDAO row, final String value) {
                    final RowUpdateWhiteboard<LeaderboardRowDAO> whiteboard = new RowUpdateWhiteboard<LeaderboardRowDAO>(
                            EditableLeaderboardPanel.this.getData());
                    getWhiteboardOwner().whiteboardProduced(whiteboard);
                    getSailingService().updateLeaderboardMaxPointsReason(getLeaderboardName(), row.competitor.name,
                            raceName, value == null || value.trim().length() == 0 ? null : value.trim(),
                            getLeaderboardDisplayDate(), new AsyncCallback<Pair<Integer, Integer>>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    getErrorReporter().reportError(
                                            "Error trying to update max points reason for competitor "
                                                    + row.competitor.name + " in leaderboard " + getLeaderboardName()
                                                    + ": " + t.getMessage() + "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Pair<Integer, Integer> newNetAndTotalPoints) {
                                    row.fieldsByRaceName.get(raceName).reasonForMaxPoints = value == null
                                            || value.length() == 0 ? null : value.trim();
                                    row.fieldsByRaceName.get(raceName).netPoints = newNetAndTotalPoints.getA();
                                    row.fieldsByRaceName.get(raceName).totalPoints = newNetAndTotalPoints.getB();
                                    getCell().setViewData(row, null); // ensure that getValue() is called again
                                    whiteboard.setObjectWithWhichToUpdateRow(row);
                                }
                            });
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            LeaderboardEntryDAO leaderboardEntryDAO = object.fieldsByRaceName.get(raceName);
            String reasonForMaxPoints = null;
            if (leaderboardEntryDAO != null) {
                reasonForMaxPoints = leaderboardEntryDAO.reasonForMaxPoints;
            }
            return reasonForMaxPoints == null ? "" : reasonForMaxPoints;
        }
    }
    
    private class NetPointsEditCellProvider extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDAO, String> {
        private final EditTextCell netPointsEditCell;
        private final String raceName;
        
        protected NetPointsEditCellProvider(String raceName) {
            this.raceName = raceName;
            netPointsEditCell = new EditTextCell();
        }
        
        @Override
        public EditTextCell getCell() {
            return netPointsEditCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDAO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDAO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDAO row, final String value) {
                    final RowUpdateWhiteboard<LeaderboardRowDAO> whiteboard = new RowUpdateWhiteboard<LeaderboardRowDAO>(
                            EditableLeaderboardPanel.this.getData());
                    getWhiteboardOwner().whiteboardProduced(whiteboard);
                    getSailingService().updateLeaderboardScoreCorrection(getLeaderboardName(), row.competitor.name, raceName,
                            value == null || value.trim().length() == 0 ? null : Integer.valueOf(value.trim()), getLeaderboardDisplayDate(),
                    new AsyncCallback<Pair<Integer, Integer>>() {
                        @Override
                        public void onFailure(Throwable t) {
                            getErrorReporter().reportError("Error trying to update score correction for competitor "+
                                    row.competitor.name+" in leaderboard "+getLeaderboardName()+
                                    " for race "+raceName+": "+t.getMessage()+
                                    "\nYou may have to refresh your view.");
                        }

                        @Override
                        public void onSuccess(Pair<Integer, Integer> newNetAndTotalPoints) {
                            row.fieldsByRaceName.get(raceName).netPoints = value==null||value.length()==0 ? newNetAndTotalPoints.getA() : Integer.valueOf(value.trim());
                            row.fieldsByRaceName.get(raceName).totalPoints = newNetAndTotalPoints.getB();
                            getCell().setViewData(row, null); // ensure that getValue() is called again
                            whiteboard.setObjectWithWhichToUpdateRow(row);
                        }
                    });
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDAO object) {
            LeaderboardEntryDAO leaderboardEntryDAO = object.fieldsByRaceName.get(raceName);
            String result = "";
            if (leaderboardEntryDAO != null) {
                result = result+leaderboardEntryDAO.netPoints;
            }
            return result;
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
    protected CompetitorColumn createCompetitorColumn() {
        return new EditableCompetitorColumn();
    }

    /*
    @Override
    protected RaceColumn<?> createRaceColumn(Entry<String, Pair<Boolean, Boolean>> raceNameAndMedalRace) {
        return new EditableRaceColumn(raceNameAndMedalRace.getKey(), raceNameAndMedalRace.getValue().getA(),
                getCellList(raceNameAndMedalRace.getKey(), raceNameAndMedalRace.getValue().getA()));
    }
    */
    
    @Override
    protected RaceColumn<?> createRaceColumn(String raceName, boolean isMedalRace, boolean isTracked) {
        return new EditableRaceColumn(raceName, isMedalRace,
                getCellList(raceName, isMedalRace));
    }

    private List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDAO, ?>> getCellList(String raceName, boolean medalRace) {
        List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDAO, ?>> list =
                new ArrayList<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDAO, ?>>();
        list.add(new MaxPointsDropDownCellProvider(raceName));
        list.add(new NetPointsEditCellProvider(raceName));
        return list;
    }

}
