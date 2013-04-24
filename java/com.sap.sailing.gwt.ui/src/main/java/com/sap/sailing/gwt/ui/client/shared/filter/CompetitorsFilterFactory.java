package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterFactory {
    public static Filter<CompetitorDTO, ?> getFilter(String filterName) {
        Filter<CompetitorDTO, ?> filter = null;
        if (filterName.equals(CompetitorTotalRankFilter.FILTER_NAME)) {
            filter = new CompetitorTotalRankFilter();
        } else if (filterName.equals(CompetitorNationalityFilter.FILTER_NAME)) {
            filter = new CompetitorNationalityFilter();
        }
        return filter;
    }
}
