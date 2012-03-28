package com.sap.sailing.server.operationaltransformation;



public abstract class AbstractLeaderboardColumnOperation extends AbstractLeaderboardOperation {
    private final String columnName;
    
    public AbstractLeaderboardColumnOperation(String leaderboardName, String columnName) {
        super(leaderboardName);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
    
    protected boolean affectsSameColumn(AbstractLeaderboardColumnOperation other) {
        return affectsSameLeaderboard(other) && getColumnName().equals(other.getColumnName());
    }


    @Override
    public RacingEventServiceOperation transformRemoveColumnFromLeaderboardServerOp(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardServerOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardServerOp)) {
            // skip server's remove and hence only apply the client's remove operation
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return this;
        }
    }

    @Override
    public RacingEventServiceOperation transformRemoveColumnFromLeaderboardClientOp(
            RemoveColumnFromLeaderboard removeColumnFromLeaderboardClientOp) {
        if (affectsSameColumn(removeColumnFromLeaderboardClientOp)) {
            // skip client's remove and hence only apply the server's remove operation
            return AbstractRacingEventServiceOperation.getNoOp();
        } else {
            return this;
        }
    }
}
