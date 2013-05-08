package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.domain.common.filter.TextOperator;
import com.sap.sailing.domain.common.filter.ValueFilter;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class ValueFilterJsonDeSerializerUtil {
    public static final String FIELD_FILTER_NAME = "name";
    public static final String FIELD_FILTER_OPERATOR = "operator";
    public static final String FIELD_FILTER_VALUE = "value";
    public static final String FIELD_FILTER_TYPE = "type";
    
    public static final String VALUE_FILTER_TYPE = "ValueFilter";

    public static JSONObject serialize(ValueFilter<?, ?> filter) {
        JSONObject filterAsJsonObject = new JSONObject();

        filterAsJsonObject.put(FIELD_FILTER_NAME, new JSONString(filter.getName()));
        filterAsJsonObject.put(FIELD_FILTER_OPERATOR, new JSONString(filter.getOperator().getName()));
        filterAsJsonObject.put(FIELD_FILTER_VALUE, new JSONString(filter.getValue().toString()));
        filterAsJsonObject.put(FIELD_FILTER_TYPE, new JSONString(VALUE_FILTER_TYPE));

        return filterAsJsonObject;
    }

    public static FilterWithUI<CompetitorDTO> deserialize(JSONObject filterAsJsonObject) {
        JSONString filterNameValue = (JSONString) filterAsJsonObject.get(FIELD_FILTER_NAME);
        JSONString filterOperatorValue = (JSONString) filterAsJsonObject.get(FIELD_FILTER_OPERATOR);
        JSONString filterValueValue = (JSONString) filterAsJsonObject.get(FIELD_FILTER_VALUE);

        String filterName = filterNameValue.stringValue();
        String operator = filterOperatorValue.stringValue();
        String value = filterValueValue.stringValue();
        
        if (filterName.equals(CompetitorTotalRankFilter.FILTER_NAME)) {
            CompetitorTotalRankFilter filter = new CompetitorTotalRankFilter();
            if(operator != null && value != null) {
                filter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.valueOf(operator)));
                filter.setValue(Integer.valueOf(value));
            }
            return filter;
        } else if (filterName.equals(CompetitorNationalityFilter.FILTER_NAME)) {
            CompetitorNationalityFilter filter = new CompetitorNationalityFilter();
            if(operator != null && value != null) {
                filter.setOperator(new TextOperator(TextOperator.Operators.valueOf(operator)));
                filter.setValue(value);
            }
            return filter;
        } else if (filterName.equals(CompetitorRaceRankFilter.FILTER_NAME)) {
            CompetitorRaceRankFilter filter = new CompetitorRaceRankFilter();
            if(operator != null && value != null) {
                filter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.valueOf(operator)));
                filter.setValue(Integer.valueOf(value));
            }
            return filter;
        } else if (filterName.equals(CompetitorSailNumbersFilter.FILTER_NAME)) {
            CompetitorSailNumbersFilter filter = new CompetitorSailNumbersFilter();
            if(operator != null && value != null) {
                filter.setOperator(new TextOperator(TextOperator.Operators.valueOf(operator)));
                filter.setValue(value);
            }
            return filter;
        } 
        return null;
    }
}
