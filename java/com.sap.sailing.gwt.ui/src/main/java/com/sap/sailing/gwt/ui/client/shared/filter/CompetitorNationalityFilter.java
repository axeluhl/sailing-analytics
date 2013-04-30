package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.TextOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorNationalityFilter extends AbstractCompetitorTextFilterWithUI {
    public static final String FILTER_NAME = "CompetitorNationalityFilter";

    public CompetitorNationalityFilter() {
        super(TextOperator.Operators.Equals);
        
        supportedOperators.add(TextOperator.Operators.Equals);
        supportedOperators.add(TextOperator.Operators.NotEqualTo);
        supportedOperators.add(TextOperator.Operators.Contains);
        supportedOperators.add(TextOperator.Operators.NotContains);
    }

    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        boolean result = false;
        if(value != null && operator != null) {
            switch (operator.getOperator()) {
            case Contains:
            case Equals:
                if(value.length() == 2 && competitor.twoLetterIsoCountryCode != null && 
                    competitor.twoLetterIsoCountryCode.equalsIgnoreCase(value)) {
                    result = true;
                } else if(value.length() == 3 && competitor.threeLetterIocCountryCode != null && 
                        competitor.threeLetterIocCountryCode.equalsIgnoreCase(value)) {
                    result = true;
                }
                break;
            case NotContains:
            case NotEqualTo:
                if(value.length() == 2 && competitor.twoLetterIsoCountryCode != null && 
                    !competitor.twoLetterIsoCountryCode.equalsIgnoreCase(value)) {
                    result = true;
                } else if(value.length() == 3 && competitor.threeLetterIocCountryCode != null && 
                        !competitor.threeLetterIocCountryCode.equalsIgnoreCase(value)) {
                    result = true;
                }
                break;
            case EndsWith:
            case StartsWith:
                throw new RuntimeException("Operator " + operator.getOperator().name() + " is not supported."); 
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
        if(value == null) {
            errorMessage = stringMessages.pleaseEnterAValue();
        } else if (value.length() != 2 && value.length() != 3) {
            errorMessage = stringMessages.nationalityMustBeISOorIOCcode(); 
        }
        return errorMessage;
    }
    
    @Override
    public ValueFilterWithUI<CompetitorDTO, String> createFilterFromWidgets(Widget valueInputWidget, Widget operatorSelectionWidget) {
        ValueFilterWithUI<CompetitorDTO, String> result = null;
        if(valueInputWidget instanceof TextBox && operatorSelectionWidget instanceof ListBox) {
            result = new CompetitorNationalityFilter();

            TextBox valueInputWidgetTextBox = (TextBox) valueInputWidget;
            ListBox operatorSelectionListBox = (ListBox) operatorSelectionWidget;
            TextOperator.Operators op = TextOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            TextOperator textOperator = new TextOperator(op);
            String value = valueInputWidgetTextBox.getValue();
            result.setOperator(textOperator);
            result.setValue(value);
        }
        
        return result;
    }

    @Override
    public Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        TextBox valueInputWidget = dataEntryDialog.createTextBox(value);
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
