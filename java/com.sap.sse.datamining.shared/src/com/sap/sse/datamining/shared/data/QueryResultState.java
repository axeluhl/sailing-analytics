package com.sap.sse.datamining.shared.data;

public enum QueryResultState {
    
    /**
     * The query finished normally and the result is reliable.
     */
    NORMAL,
    
    /**
     * The Query timed out.<br />
     * The result can be empty or incomplete.
     */
    TIMED_OUT,
    
    /**
     * The query was aborted.<br />
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
    ERROR

}
