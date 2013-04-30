package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class FilterSetWithUI<FilterObjectType> extends FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>> {

    public FilterSetWithUI(String name) {
        super(name);
    }
}
