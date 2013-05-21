package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * A specialized filter which can create a UI factory for making a filter editable  
 * @author Frank
 */
public interface FilterWithUI<FilterObjectType> extends Filter<FilterObjectType> {
    String validate(StringMessages stringMessages);
    
    String getLocalizedName(StringMessages stringMessages);
    String getLocalizedDescription(StringMessages stringMessages);
    
    FilterWithUI<FilterObjectType> copy();
    
    FilterUIFactory<FilterObjectType> createUIFactory();
}
