package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.filter.TextOperator;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorNationalityFilter extends AbstractCompetitorTextFilterWithUI {
    public static final String FILTER_NAME = "CompetitorNationalityFilter";

    private TextBox valueTextBox;
    private ListBox operatorSelectionListBox;

    public CompetitorNationalityFilter() {
        super(TextOperator.Operators.Equals);
        
        supportedOperators.add(TextOperator.Operators.Equals);
        supportedOperators.add(TextOperator.Operators.NotEqualTo);
        supportedOperators.add(TextOperator.Operators.Contains);
        supportedOperators.add(TextOperator.Operators.NotContains);
        
        valueTextBox = null;
        operatorSelectionListBox = null;
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
    public String getLocalizedDescription(StringMessages stringMessages) {
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
    public Widget createFilterUIWidget(DataEntryDialog<?> dataEntryDialog) {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(createOperatorSelectionWidget(dataEntryDialog));
        hp.add(createValueInputWidget(dataEntryDialog));
        hp.setSpacing(5);
        return hp;
    }

    private Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        if(valueTextBox == null) {
            valueTextBox = dataEntryDialog.createTextBox(value);
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
    public FilterWithUI<CompetitorDTO> copy() {
        CompetitorNationalityFilter result = new CompetitorNationalityFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }
    
    @Override
    public FilterWithUI<CompetitorDTO> createFilterFromUIWidget() {
        CompetitorNationalityFilter result = null;

        if(valueTextBox != null && operatorSelectionListBox != null) {
            result = new CompetitorNationalityFilter();

            TextOperator.Operators op = TextOperator.Operators.valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            TextOperator textOperator = new TextOperator(op);
            result.setOperator(textOperator);
            result.setValue(valueTextBox.getValue());
        }
        return result;
    }
}
