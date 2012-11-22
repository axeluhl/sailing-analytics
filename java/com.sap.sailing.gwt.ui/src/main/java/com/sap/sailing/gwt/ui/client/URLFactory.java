package com.sap.sailing.gwt.ui.client;

public interface URLFactory {

    public static final URLFactoryImpl INSTANCE = new URLFactoryImpl();
    
    public String encode(String url);
    
}
