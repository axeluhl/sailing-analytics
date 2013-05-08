package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.filter.AbstractNumberFilter;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorNumberFilterWithUI<T extends Number> extends AbstractNumberFilter<CompetitorDTO, T> 
    implements FilterWithUI<CompetitorDTO> {
    
    protected List<BinaryOperator.Operators> supportedOperators;
    protected BinaryOperator.Operators defaultOperator;
    
    public AbstractCompetitorNumberFilterWithUI(BinaryOperator.Operators defaultOperator) {
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<BinaryOperator.Operators>();
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for(BinaryOperator.Operators op: supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if(operator != null && operator.getName().equals(op.name())) {
                operatorsListBox.setSelectedIndex(i);
            } else if (defaultOperator != null && defaultOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        return operatorsListBox;
    }
}
