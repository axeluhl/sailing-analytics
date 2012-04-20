package com.sap.sailing.server.operationaltransformation;



public abstract class AbstractLeaderboardColumnOperation<ResultType> extends AbstractLeaderboardOperation<ResultType> {
    private static final long serialVersionUID = 8577267743945864388L;
    private final String columnName;
    
    public AbstractLeaderboardColumnOperation(String leaderboardName, String columnName) {
        super(leaderboardName);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
    
    protected boolean affectsSameColumn(AbstractLeaderboardColumnOperation<?> other) {
        return affectsSameLeaderboard(other) && getColumnName().equals(other.getColumnName());
    }

}
