package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CompetitorRaceRankFilterUIFactory extends AbstractCompetitorNumberFilterUIFactory<Integer> { 
    private IntegerBox valueIntegerBox;
    private ListBox operatorSelectionListBox;

    public CompetitorRaceRankFilterUIFactory() {
        this(new CompetitorRaceRankFilter());
    }

    public CompetitorRaceRankFilterUIFactory(CompetitorRaceRankFilter competitorRaceTotalPointsFilter) {
        super(competitorRaceTotalPointsFilter, BinaryOperator.Operators.LessThanEquals);
        
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
        Grid hpGrid = new Grid(1, 2);
        hpGrid.setWidget(0, 0, createOperatorSelectionWidget(dataEntryDialog));
        hpGrid.setWidget(0, 1, createValueInputWidget(dataEntryDialog));
        hpGrid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        hpGrid.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
        return hpGrid;
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
    public CompetitorRaceRankFilter createFilterFromUI() {
        CompetitorRaceRankFilter result = null;
        if(valueIntegerBox != null && operatorSelectionListBox != null) {
            result = new CompetitorRaceRankFilter();

            BinaryOperator.Operators op = BinaryOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            BinaryOperator<Integer> binaryOperator = new BinaryOperator<Integer>(op);
            
            Integer value = valueIntegerBox.getValue();
            result.setOperator(binaryOperator);
            result.setValue(value);
        }
        return result;
    }
}
