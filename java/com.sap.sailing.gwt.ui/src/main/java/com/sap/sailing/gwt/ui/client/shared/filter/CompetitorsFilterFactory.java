package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.filter.BinaryOperator;
import com.sap.sailing.domain.common.filter.TextOperator;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterFactory {
    public static ValueFilterWithUI<CompetitorDTO, ?> getFilter(String filterName) {
        ValueFilterWithUI<CompetitorDTO, ?> filter = null;
        if (filterName.equals(CompetitorTotalRankFilter.FILTER_NAME)) {
            filter = new CompetitorTotalRankFilter();
        } else if (filterName.equals(CompetitorNationalityFilter.FILTER_NAME)) {
            filter = new CompetitorNationalityFilter();
        } else if (filterName.equals(CompetitorRaceRankFilter.FILTER_NAME)) {
            filter = new CompetitorRaceRankFilter();
        } 
        return filter;
    }

    public static ValueFilterWithUI<CompetitorDTO, ?> getFilter(String filterName, String operator, String value) {
        if (filterName.equals(CompetitorTotalRankFilter.FILTER_NAME)) {
            CompetitorTotalRankFilter filter = new CompetitorTotalRankFilter();
            filter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.valueOf(operator)));
            filter.setValue(Integer.valueOf(value));
            return filter;
        } else if (filterName.equals(CompetitorNationalityFilter.FILTER_NAME)) {
            CompetitorNationalityFilter filter = new CompetitorNationalityFilter();
            filter.setOperator(new TextOperator(TextOperator.Operators.valueOf(operator)));
            filter.setValue(value);
            return filter;
        } else if (filterName.equals(CompetitorRaceRankFilter.FILTER_NAME)) {
            CompetitorRaceRankFilter filter = new CompetitorRaceRankFilter();
            filter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.valueOf(operator)));
            filter.setValue(Integer.valueOf(value));
            return filter;
        } 
        return null;
    }

}
