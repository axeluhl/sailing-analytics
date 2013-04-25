package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorTotalRankFilter extends AbstractCompetitorInLeaderboardFilter<Integer> { 
    public static final String FILTER_NAME = "CompetitorTotalRankFilter";

    private IntegerBox valueInputWidget;
    private ListBox operatorSelectionWidget;

    static {
        supportedOperators = new ArrayList<FilterOperators>();
        supportedOperators.add(FilterOperators.LessThanEquals);
        supportedOperators.add(FilterOperators.GreaterThanEquals);
        supportedOperators.add(FilterOperators.LessThan);
        supportedOperators.add(FilterOperators.GreaterThan);
        supportedOperators.add(FilterOperators.NotEqualTo);
        supportedOperators.add(FilterOperators.Equals);
    }

    public CompetitorTotalRankFilter() {
        super(FilterOperators.GreaterThanEquals);
    }

    public Class<Integer> getValueType() {
        return Integer.class;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        
        if (filterValue > 0 && filterOperator != null && getLeaderboard() != null) {
            int totalRank = getLeaderboard().getRank(competitorDTO);
            switch (filterOperator) {
                case LessThanEquals:
                    result = totalRank <= filterValue;
                    break;
                case Equals:
                    result = totalRank == filterValue;
                    break;
                case GreaterThanEquals:
                    result = totalRank >= filterValue;
                    break;
                case LessThan:
                    result = totalRank < filterValue;
                    break;
                case GreaterThan:
                    result = totalRank > filterValue;
                    break;
                case NotEqualTo:
                    result = totalRank != filterValue;
                    break;
                case NotContains:
                case StartsWith:
                case Contains:
                case EndsWith:
                default:
                    throw new RuntimeException("Operator " + filterOperator.name() + " is not supported."); 
            }
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
        valueInputWidget = dataEntryDialog.createIntegerBox(filterValue, 20);
        valueInputWidget.setFocus(true);
        return valueInputWidget;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(filterValue != null) {
            Integer intfilterValue = (Integer) filterValue;
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
    public Filter<CompetitorDTO, Integer> createFilterFromWidgets(Widget valueInputWidget, Widget operatorSelectionWidget) {
        Filter<CompetitorDTO, Integer> result = null;
        if(valueInputWidget instanceof IntegerBox && operatorSelectionWidget instanceof ListBox) {
            result = new CompetitorTotalRankFilter();
            IntegerBox valueInputIntegerBox = (IntegerBox) valueInputWidget;
            ListBox operatorSelectionListBox = (ListBox) operatorSelectionWidget;

            FilterOperators op = FilterOperators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            Integer value = valueInputIntegerBox.getValue();
            result.setConfiguration(new Pair<FilterOperators, Integer>(op, value));
        }
        return result;
    }
}
