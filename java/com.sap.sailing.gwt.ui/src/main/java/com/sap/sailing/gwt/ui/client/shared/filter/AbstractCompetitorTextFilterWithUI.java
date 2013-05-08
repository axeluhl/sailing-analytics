package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.filter.AbstractTextFilter;
import com.sap.sailing.domain.common.filter.TextOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorTextFilterWithUI extends AbstractTextFilter<CompetitorDTO> 
    implements FilterWithUI<CompetitorDTO> {
    
    protected List<TextOperator.Operators> supportedOperators;
    protected TextOperator.Operators defaultOperator;

    public AbstractCompetitorTextFilterWithUI(TextOperator.Operators defaultOperator) {
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<TextOperator.Operators>();
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for(TextOperator.Operators op: supportedOperators) {
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
