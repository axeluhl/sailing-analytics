package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorTotalRankFilter extends AbstractCompetitorNumberFilterWithUI<Integer> { 
    public static final String FILTER_NAME = "CompetitorTotalRankFilter";

    private IntegerBox valueInputWidget;
    private ListBox operatorSelectionWidget;

    public CompetitorTotalRankFilter() {
        super(BinaryOperator.Operators.LessThanEquals);
        
        supportedOperators.add(BinaryOperator.Operators.LessThanEquals);
        supportedOperators.add(BinaryOperator.Operators.GreaterThanEquals);
        supportedOperators.add(BinaryOperator.Operators.LessThan);
        supportedOperators.add(BinaryOperator.Operators.GreaterThan);
        supportedOperators.add(BinaryOperator.Operators.NotEqualTo);
        supportedOperators.add(BinaryOperator.Operators.Equals);
    }

    public Class<Integer> getValueType() {
        return Integer.class;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        
        if (value > 0 && operator != null && getLeaderboard() != null) {
            int totalRank = getLeaderboard().getRank(competitorDTO);
            result = operator.matchValues(value, totalRank);
        }
        
        return result;
    }
        
    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.totalRank();
    }

    @Override
    public Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        valueInputWidget = dataEntryDialog.createIntegerBox(value, 20);
        valueInputWidget.setFocus(true);
        return valueInputWidget;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(value != null) {
            Integer intfilterValue = (Integer) value;
            if(intfilterValue <= 0) {
                errorMessage = stringMessages.numberMustBePositive();
            }
        } else {
            errorMessage = stringMessages.pleaseEnterANumber();
        }
        return errorMessage;
    }
    
    @Override
    public Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        operatorSelectionWidget = createOperatorSelectionListBox(dataEntryDialog);
        return operatorSelectionWidget;
    }

    @Override
    public ValueFilterWithUI<CompetitorDTO, Integer> createFilterFromWidgets(Widget valueInputWidget, Widget operatorSelectionWidget) {
        ValueFilterWithUI<CompetitorDTO, Integer> result = null;
        if(valueInputWidget instanceof IntegerBox && operatorSelectionWidget instanceof ListBox) {
            result = new CompetitorTotalRankFilter();
            IntegerBox valueInputIntegerBox = (IntegerBox) valueInputWidget;
            ListBox operatorSelectionListBox = (ListBox) operatorSelectionWidget;

            BinaryOperator.Operators op = BinaryOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            BinaryOperator<Integer> binaryOperator = new BinaryOperator<Integer>(op);
            
            Integer value = valueInputIntegerBox.getValue();
            result.setOperator(binaryOperator);
            result.setValue(value);
        }
        return result;
    }
}
