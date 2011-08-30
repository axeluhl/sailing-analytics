package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
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

}
