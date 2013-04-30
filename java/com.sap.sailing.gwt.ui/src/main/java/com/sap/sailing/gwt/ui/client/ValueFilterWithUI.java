package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.ValueFilter;

/**
 * A specialized filter creating a UI input widget for the value field  
 * @author Frank
 */
public interface ValueFilterWithUI<FilterObjectType, ValueType> extends ValueFilter<FilterObjectType, ValueType> {
    Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog);
    Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog);

    ValueFilter<FilterObjectType, ValueType> createFilterFromWidgets(Widget valueInputWidget, Widget operatorSelectionWidget);
    
    String validate(StringMessages stringMessages);
    String getLocalizedName(StringMessages stringMessages);
}
