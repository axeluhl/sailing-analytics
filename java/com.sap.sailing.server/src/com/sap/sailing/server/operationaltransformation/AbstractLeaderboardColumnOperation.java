package com.sap.sailing.server.operationaltransformation;


public abstract class AbstractLeaderboardColumnOperation extends AbstractRacingEventServiceOperation {
    private final String leaderboardName;
    private final String columnName;
    
    public AbstractLeaderboardColumnOperation(String leaderboardName, String columnName) {
        super();
        this.leaderboardName = leaderboardName;
        this.columnName = columnName;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public String getColumnName() {
        return columnName;
    }
    
    protected boolean affectsSameColumn(AbstractLeaderboardColumnOperation other) {
        return getLeaderboardName().equals(other.getLeaderboardName()) && getColumnName().equals(other.getColumnName());
    }
}
