package com.sap.sailing.gwt.home.shared.partials.filter;

import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaByBootCategoryFilter extends AbstractSelectionFilter<RegattaMetadataDTO, String> {
    
    @Override
    protected String getFilterCriteria(RegattaMetadataDTO object) {
        return object.getBoatCategory();
    }
    
    @Override
    protected String getFilterItemLabel(String item) {
        return item == null ? StringMessages.INSTANCE.all() : item;
    }
    
}
