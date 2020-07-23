package com.sap.sailing.gwt.home.desktop.partials.sharing;

import com.sap.sailing.gwt.home.shared.places.ShareablePlaceContext;

public interface SharingMetadataProvider {
    
    String getShortText();
    
    String getLongText(String url);
    
    ShareablePlaceContext getContext();

}
