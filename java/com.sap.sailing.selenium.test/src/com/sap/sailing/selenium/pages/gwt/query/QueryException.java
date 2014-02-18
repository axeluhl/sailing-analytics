package com.sap.sailing.selenium.pages.gwt.query;

public class QueryException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public QueryException() {
        super();
    }
    
    public QueryException(String message) {
        super(message);
    }
    
    public QueryException(Throwable cause) {
        super(cause);
    }
    
    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
