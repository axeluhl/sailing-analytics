package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.filter.TextFilter;
import com.sap.sse.common.filter.TextOperator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractCompetitorTextFilterUIFactory implements FilterUIFactory<CompetitorWithBoatDTO> {
    
    protected List<TextOperator.Operators> supportedOperators;
    protected TextOperator.Operators defaultOperator;
    protected TextFilter<CompetitorWithBoatDTO> competitorTextFilter;

    public AbstractCompetitorTextFilterUIFactory(TextFilter<CompetitorWithBoatDTO> competitorTextFilter, TextOperator.Operators defaultOperator) {
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
