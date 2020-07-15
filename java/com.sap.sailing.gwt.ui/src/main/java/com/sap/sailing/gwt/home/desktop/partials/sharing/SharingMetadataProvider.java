package com.sap.sailing.gwt.home.desktop.partials.sharing;

public interface SharingMetadataProvider {
    
    String getTitle();
    
    String getDescription();
    
    String getShortText();
    
    String getLongText(String url);
    
    String getImageUrl();
}
