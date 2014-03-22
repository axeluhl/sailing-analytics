package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.AbstractTextFilter;

/**
 * A filter filtering competitors by their nationality
 * @author Frank
 *
 */
public class CompetitorNationalityFilter extends AbstractTextFilter<CompetitorDTO> implements FilterWithUI<CompetitorDTO> {
    public static final String FILTER_NAME = "CompetitorNationalityFilter";

    public CompetitorNationalityFilter() {
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        boolean result = false;
        if(value != null && operator != null) {
            switch (operator.getOperator()) {
            case Contains:
            case Equals:
                if(value.length() == 2 && competitor.getTwoLetterIsoCountryCode() != null && 
                    competitor.getTwoLetterIsoCountryCode().equalsIgnoreCase(value)) {
                    result = true;
                } else if(value.length() == 3 && competitor.getThreeLetterIocCountryCode() != null && 
                        competitor.getThreeLetterIocCountryCode().equalsIgnoreCase(value)) {
                    result = true;
                }
                break;
            case NotContains:
            case NotEqualTo:
                if(value.length() == 2 && competitor.getTwoLetterIsoCountryCode() != null && 
                    !competitor.getTwoLetterIsoCountryCode().equalsIgnoreCase(value)) {
                    result = true;
                } else if(value.length() == 3 && competitor.getThreeLetterIocCountryCode() != null && 
                        !competitor.getThreeLetterIocCountryCode().equalsIgnoreCase(value)) {
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
    public CompetitorNationalityFilter copy() {
        CompetitorNationalityFilter result = new CompetitorNationalityFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return new CompetitorNationalityFilterUIFactory(this);
    }
}
