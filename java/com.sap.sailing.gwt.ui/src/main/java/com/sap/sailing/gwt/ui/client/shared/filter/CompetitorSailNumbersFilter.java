package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.AbstractTextFilter;

/**
 * A filter filtering competitors by their sail number
 * @author Frank
 *
 */
public class CompetitorSailNumbersFilter extends AbstractTextFilter<CompetitorDTO> implements FilterWithUI<CompetitorDTO> {
    public static final String FILTER_NAME = "CompetitorSailNumbersFilter";

    public CompetitorSailNumbersFilter() {
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        boolean result = false;
        final String sailId;
        if (value != null && operator != null && competitor.hasBoat() && (sailId=((CompetitorWithBoatDTO) competitor).getSailID()) != null) {
            switch (operator.getOperator()) {
            case Contains:
            case NotContains:
                result = operator.matchValues(sailId, value);
                break;
            case Equals:
            case NotEqualTo:
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
        return stringMessages.sailNumber();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return stringMessages.sailNumber();
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(value == null) {
            errorMessage = stringMessages.pleaseEnterAValue();
        }
        return errorMessage;
    }

    @Override
    public CompetitorSailNumbersFilter copy() {
        CompetitorSailNumbersFilter result = new CompetitorSailNumbersFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return new CompetitorSailNumbersFilterUIFactory(this);
    }
}
