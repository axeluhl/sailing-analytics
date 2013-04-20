package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.core.shared.GWT;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CompetitorsFilterFormatter {
    private CompetitorsFilterFormatter() { }
    
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);
        
    public static String format(Filter<CompetitorDTO, ?> filter) {
        String result = null;
        if(filter instanceof CompetitorNationalityFilter) {
            result = stringMessages.nationality();
        } else if(filter instanceof CompetitorTotalRankFilter) {
            result = stringMessages.totalRank();
        }
        return result;
    }
}
