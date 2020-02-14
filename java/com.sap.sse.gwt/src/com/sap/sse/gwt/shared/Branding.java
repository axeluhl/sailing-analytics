package com.sap.sse.gwt.shared;

public class Branding {

    private static final Branding INSTANCE;
    
    static {
        INSTANCE = new Branding();
    }

    public static Branding getInstance() {
        return INSTANCE;
    }

    /**
     * 
     * @return true when 
     */
    public boolean isActive() {
        return false;
    }
}
