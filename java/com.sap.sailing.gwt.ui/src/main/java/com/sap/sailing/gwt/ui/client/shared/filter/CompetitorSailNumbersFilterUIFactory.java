package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.filter.TextOperator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CompetitorSailNumbersFilterUIFactory extends AbstractCompetitorTextFilterUIFactory {
    private TextBox valueTextBox;
    private ListBox operatorSelectionListBox;

    public CompetitorSailNumbersFilterUIFactory() {
        this(new CompetitorSailNumbersFilter());
    }
    
    public CompetitorSailNumbersFilterUIFactory(CompetitorSailNumbersFilter competitorSailNumbersFilter) {
        super(competitorSailNumbersFilter, TextOperator.Operators.Contains);
        
        supportedOperators.add(TextOperator.Operators.Contains);
        supportedOperators.add(TextOperator.Operators.NotContains);
        
        valueTextBox = null;
        operatorSelectionListBox = null;
    }

    @Override 
    public Widget createFilterUIWidget(DataEntryDialog<?> dataEntryDialog) {
        Grid hpGrid = new Grid(1, 2);
        hpGrid.setWidget(0, 0, createOperatorSelectionWidget(dataEntryDialog));
        hpGrid.setWidget(0, 1, createValueInputWidget(dataEntryDialog));
        hpGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        hpGrid.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
        return hpGrid;
    }

    private Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        if(valueTextBox == null) {
            valueTextBox = dataEntryDialog.createTextBox(competitorTextFilter.getValue());
            valueTextBox.setVisibleLength(20);
            valueTextBox.setFocus(true);
        }
        return valueTextBox;
    }

    private Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        if(operatorSelectionListBox == null) {
            operatorSelectionListBox = createOperatorSelectionListBox(dataEntryDialog);
        }
        return operatorSelectionListBox;
    }
    
    @Override
    public FilterWithUI<CompetitorDTO> createFilterFromUI() {
        CompetitorSailNumbersFilter result = null;
        if (valueTextBox != null && operatorSelectionListBox != null) {
            result = new CompetitorSailNumbersFilter();
            TextOperator.Operators op = TextOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            TextOperator textOperator = new TextOperator(op);
            result.setOperator(textOperator);
            result.setValue(valueTextBox.getValue());
        }
        return result;
    }
}
