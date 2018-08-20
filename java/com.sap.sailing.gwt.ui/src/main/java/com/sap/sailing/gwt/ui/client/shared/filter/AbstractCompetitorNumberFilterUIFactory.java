package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.NumberFilter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractCompetitorNumberFilterUIFactory<T extends Number> implements FilterUIFactory<CompetitorDTO> {
    protected List<BinaryOperator.Operators> supportedOperators;
    protected BinaryOperator.Operators defaultOperator;
    protected NumberFilter<CompetitorDTO, T> competitorNumberFilter;

    public AbstractCompetitorNumberFilterUIFactory(NumberFilter<CompetitorDTO, T> competitorNumberFilter, BinaryOperator.Operators defaultOperator) {
        this.competitorNumberFilter = competitorNumberFilter;
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<BinaryOperator.Operators>();
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for (BinaryOperator.Operators op : supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if (competitorNumberFilter.getOperator() != null && competitorNumberFilter.getOperator().getName().equals(op.name())) {
                operatorsListBox.setSelectedIndex(i);
            } else if (defaultOperator != null && defaultOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        return operatorsListBox;
    }
}
