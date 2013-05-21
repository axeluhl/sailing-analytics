package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.filter.TextFilter;
import com.sap.sailing.domain.common.filter.TextOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public abstract class AbstractCompetitorTextFilterUIFactory implements FilterUIFactory<CompetitorDTO> {
    
    protected List<TextOperator.Operators> supportedOperators;
    protected TextOperator.Operators defaultOperator;
    protected TextFilter<CompetitorDTO> competitorTextFilter;

    public AbstractCompetitorTextFilterUIFactory(TextFilter<CompetitorDTO> competitorTextFilter, TextOperator.Operators defaultOperator) {
        this.competitorTextFilter = competitorTextFilter;
        this.defaultOperator = defaultOperator;
        supportedOperators = new ArrayList<TextOperator.Operators>();
    }

    protected ListBox createOperatorSelectionListBox(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorsListBox = dataEntryDialog.createListBox(false);
        int i = 0;
        for(TextOperator.Operators op: supportedOperators) {
            operatorsListBox.addItem(FilterOperatorsFormatter.format(op), op.name());
            if(competitorTextFilter.getOperator() != null && competitorTextFilter.getOperator().getName().equals(op.name())) {
                operatorsListBox.setSelectedIndex(i);
            } else if (defaultOperator != null && defaultOperator.equals(op)) {
                operatorsListBox.setSelectedIndex(i);
            }
            i++;
        }
        return operatorsListBox;
    }
}
