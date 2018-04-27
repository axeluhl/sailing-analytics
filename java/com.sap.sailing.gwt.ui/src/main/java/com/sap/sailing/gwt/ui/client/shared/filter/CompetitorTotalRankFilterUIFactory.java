package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CompetitorTotalRankFilterUIFactory extends AbstractCompetitorNumberFilterUIFactory<Integer> { 
    private IntegerBox valueIntegerBox;
    private ListBox operatorSelectionListBox;

    public CompetitorTotalRankFilterUIFactory() {
        this(new CompetitorTotalRankFilter());
    }
    
    public CompetitorTotalRankFilterUIFactory(CompetitorTotalRankFilter competitorTotalRankFilter) {
        super(competitorTotalRankFilter, BinaryOperator.Operators.LessThanEquals);
        
        supportedOperators.add(BinaryOperator.Operators.LessThanEquals);
        supportedOperators.add(BinaryOperator.Operators.GreaterThanEquals);
        supportedOperators.add(BinaryOperator.Operators.LessThan);
        supportedOperators.add(BinaryOperator.Operators.GreaterThan);
        supportedOperators.add(BinaryOperator.Operators.NotEqualTo);
        supportedOperators.add(BinaryOperator.Operators.Equals);
        
        valueIntegerBox = null;
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
        if(valueIntegerBox == null) {
            valueIntegerBox = dataEntryDialog.createIntegerBox(competitorNumberFilter.getValue(), 20);
            valueIntegerBox.setFocus(true);
        }
        return valueIntegerBox;
    }

    private Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        if(operatorSelectionListBox == null) {
            operatorSelectionListBox = createOperatorSelectionListBox(dataEntryDialog);
        }
        return operatorSelectionListBox;
    }

    @Override
    public FilterWithUI<CompetitorDTO> createFilterFromUI() {
        CompetitorTotalRankFilter result = null;
        if(valueIntegerBox != null && operatorSelectionListBox != null) {
            result = new CompetitorTotalRankFilter();

            BinaryOperator.Operators op = BinaryOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            BinaryOperator<Integer> binaryOperator = new BinaryOperator<Integer>(op);
            
            Integer value = valueIntegerBox.getValue();
            result.setOperator(binaryOperator);
            result.setValue(value);
        }
        return result;
    }
}
