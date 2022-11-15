package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

/**
 * A factory for FilterWithUI<Competitor> instances  
 * @author Frank
 */
public class CompetitorFilterWithUIFactory {
    public static FilterWithUI<CompetitorDTO> createFilter(String filterName) {
        if (CompetitorRaceRankFilter.FILTER_NAME.equals(filterName)) {
            return new CompetitorRaceRankFilter();
        } else if (CompetitorTotalRankFilter.FILTER_NAME.equals(filterName)) {
            return new CompetitorTotalRankFilter();
        } else if (CompetitorNationalityFilter.FILTER_NAME.equals(filterName)) {
            return new CompetitorNationalityFilter();
        } else if (CompetitorSailNumbersFilter.FILTER_NAME.equals(filterName)) {
            return new CompetitorSailNumbersFilter();
        } else if (SelectedCompetitorsFilter.FILTER_NAME.equals(filterName)) {
            return new SelectedCompetitorsFilter();
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
