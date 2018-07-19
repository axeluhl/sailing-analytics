package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.NumberFilter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractTagNumberFilterUIFactory<T extends Number> implements FilterUIFactory<TagDTO> {
    protected List<BinaryOperator.Operators> supportedOperators;
    protected BinaryOperator.Operators defaultOperator;
    protected NumberFilter<TagDTO, T> tagNumberFilter;

    public AbstractTagNumberFilterUIFactory(NumberFilter<TagDTO, T> tagNumberFilter, BinaryOperator.Operators defaultOperator) {
        this.tagNumberFilter = tagNumberFilter;
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<BinaryOperator.Operators>();
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for (BinaryOperator.Operators op : supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if (tagNumberFilter.getOperator() != null && tagNumberFilter.getOperator().getName().equals(op.name())) {
                operatorsListBox.setSelectedIndex(i);
            } else if (defaultOperator != null && defaultOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        return operatorsListBox;
    }
}
