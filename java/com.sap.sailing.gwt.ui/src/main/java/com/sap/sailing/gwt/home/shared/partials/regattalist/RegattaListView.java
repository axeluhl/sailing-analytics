package com.sap.sailing.gwt.home.shared.partials.regattalist;

import java.util.Map;

import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;

public interface RegattaListView {
    
    Map<RegattaListItem, RegattaMetadataDTO> getItemMap();
    
    public interface RegattaListItem {
        void doFilter(boolean filter);
    }
}
