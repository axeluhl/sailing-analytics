package com.sap.sse.datamining;

import com.sap.sse.datamining.shared.data.QueryResultState;

public enum QueryState {
    
    /**
     * The Query hasn't been started yet.
     */
    NOT_STARTED,
    
    /**
     * The Query is currently running.
     */
    RUNNING,
    
    /**
     * The Query finished normally and the result is reliable.
     */
    NORMAL,
    
    /**
     * The Query timed out.<br />
     * The result can be empty or incomplete.
     */
    TIMED_OUT,
    
    /**
     * The Query was aborted.<br />
     * The result can be empty or incomplete.
     */
    ABORTED,
    
    /**
     * Failures occurred during the query processing.<br />
     * The result can be empty, incomplete or incorrect.
     */
    FAILURE,
    
    /**
     * A severe error occurred during the query processing, that caused the query to stop.<br />
     * The result can be empty, incomplete or incorrect.
     */
    ERROR;
    
    public QueryResultState asResultState() {
        switch (this) {
        case ABORTED:
            return QueryResultState.ABORTED;
        case ERROR:
            return QueryResultState.ERROR;
        case FAILURE:
            return QueryResultState.FAILURE;
        case NORMAL:
            return QueryResultState.NORMAL;
        case TIMED_OUT:
            return QueryResultState.TIMED_OUT;
        default:
            throw new UnsupportedOperationException("There's no equivalent " + QueryResultState.class.getSimpleName() + " for '" + this + "'");
        }
    }

}
