package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorNationalityFilter extends AbstractFilterWithUI<CompetitorDTO, String> {
    public static final String FILTER_NAME = "CompetitorNationalityFilter";

    static {
        supportedOperators = new ArrayList<FilterOperators>();
        supportedOperators.add(FilterOperators.Equals);
        supportedOperators.add(FilterOperators.NotEqualTo);
        supportedOperators.add(FilterOperators.Contains);
        supportedOperators.add(FilterOperators.NotContains);
    }
    
    public CompetitorNationalityFilter() {
        super(FilterOperators.Equals);
    }

    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        boolean result = false;
        if(filterValue != null && filterOperator != null) {
            switch (filterOperator) {
            case Contains:
            case Equals:
                if(filterValue.length() == 2 && competitor.twoLetterIsoCountryCode != null && 
                    competitor.twoLetterIsoCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                } else if(filterValue.length() == 3 && competitor.threeLetterIocCountryCode != null && 
                        competitor.threeLetterIocCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                }
                break;
            case NotContains:
            case NotEqualTo:
                if(filterValue.length() == 2 && competitor.twoLetterIsoCountryCode != null && 
                    !competitor.twoLetterIsoCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                } else if(filterValue.length() == 3 && competitor.threeLetterIocCountryCode != null && 
                        !competitor.threeLetterIocCountryCode.equalsIgnoreCase(filterValue)) {
                    result = true;
                }
                break;
            case EndsWith:
            case GreaterThan:
            case GreaterThanEquals:
            case LessThan:
            case LessThanEquals:
            case StartsWith:
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
        return stringMessages.nationality();
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(filterValue == null) {
            errorMessage = stringMessages.pleaseEnterAValue();
        } else if (filterValue.length() != 2 && filterValue.length() != 3) {
            errorMessage = stringMessages.nationalityMustBeISOorIOCcode(); 
        }
        return errorMessage;
    }
    
    @Override
    public Filter<CompetitorDTO, String> createFilterFromWidgets(Widget valueInputWidget, Widget operatorSelectionWidget) {
        Filter<CompetitorDTO, String> result = null;
        if(valueInputWidget instanceof TextBox && operatorSelectionWidget instanceof ListBox) {
            result = new CompetitorNationalityFilter();

            TextBox valueInputWidgetTextBox = (TextBox) valueInputWidget;
            ListBox operatorSelectionListBox = (ListBox) operatorSelectionWidget;
            FilterOperators op = FilterOperators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            String value = valueInputWidgetTextBox.getValue();
            result.setConfiguration(new Pair<FilterOperators, String>(op, value));
        }
        
        return result;
    }

    @Override
    public Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        TextBox valueInputWidget = dataEntryDialog.createTextBox(filterValue);
        valueInputWidget.setVisibleLength(20);
        valueInputWidget.setFocus(true);
        return valueInputWidget;
    }

    @Override
    public Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        ListBox operatorSelectionWidget = createOperatorSelectionListBox(dataEntryDialog);
        return operatorSelectionWidget;
    }
}
