package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.HorizontalPanel;
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
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(createOperatorSelectionWidget(dataEntryDialog));
        hp.add(createValueInputWidget(dataEntryDialog));
        hp.setSpacing(5);
        
        return hp;
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
