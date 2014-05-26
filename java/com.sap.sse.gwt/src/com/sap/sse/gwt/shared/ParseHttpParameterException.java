package com.sap.sse.gwt.shared;

public class ParseHttpParameterException extends Exception {
    private static final long serialVersionUID = -6999866727315263021L;
    private final String parameterName;
    
    public ParseHttpParameterException(String parameterName, String message, Throwable ex) {
        super(message, ex);
        this.parameterName = parameterName;
    }

    public ParseHttpParameterException(String parameterName, String message) {
        super(message);
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }    

}
