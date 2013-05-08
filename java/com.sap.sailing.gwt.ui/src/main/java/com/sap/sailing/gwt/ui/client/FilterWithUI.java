package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;

/**
 * A specialized filter creating a UI input widget for the filter  
 * @author Frank
 */
public interface FilterWithUI<FilterObjectType> extends Filter<FilterObjectType> {
    Widget createFilterUIWidget(DataEntryDialog<?> dataEntryDialog);

    FilterWithUI<FilterObjectType> createFilterFromUIWidget();
    FilterWithUI<FilterObjectType> copy();
    
    String validate(StringMessages stringMessages);
    String getLocalizedName(StringMessages stringMessages);
}
