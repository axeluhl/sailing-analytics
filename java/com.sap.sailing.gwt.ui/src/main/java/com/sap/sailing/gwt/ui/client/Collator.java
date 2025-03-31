package com.sap.sailing.gwt.ui.client;

public class Collator {
    private static final Collator instance = new Collator();
    
    public static final Collator getInstance() {
        return instance;
    }
    
    public native int compare(String o1, String o2) /*-{
        return o1.localeCompare(o2);
    }-*/;
}
