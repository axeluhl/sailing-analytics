package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.filter.AbstractFilter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterOperatorsFormatter;
import com.sap.sailing.gwt.ui.client.FilterWithUI;

public abstract class AbstractFilterWithUI<FilterObjectType, ValueType> extends AbstractFilter<FilterObjectType, ValueType> 
    implements FilterWithUI<FilterObjectType, ValueType> {
    
    protected List<FilterOperators> supportedOperators;
    protected FilterOperators defaultOperator;
    
    public AbstractFilterWithUI(FilterOperators defaultOperator) {
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<FilterOperators>();
    }

    @Override
    public FilterOperators getDefaultOperator() {
        return FilterOperators.GreaterThanEquals;
    }

    @Override
    public Iterable<FilterOperators> getSupportedOperators() {
        return supportedOperators;
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for(FilterOperators op: supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if(filterOperator != null && filterOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            } else if (defaultOperator != null && defaultOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        return operatorsListBox;
    }
}
