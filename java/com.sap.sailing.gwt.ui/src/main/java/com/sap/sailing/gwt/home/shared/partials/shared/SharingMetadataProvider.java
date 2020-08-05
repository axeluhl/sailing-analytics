package com.sap.sailing.gwt.home.shared.partials.shared;

import com.sap.sailing.gwt.home.shared.places.ShareablePlaceContext;

public interface SharingMetadataProvider {
    
    String getShortText();
    
    ShareablePlaceContext getContext();
}
