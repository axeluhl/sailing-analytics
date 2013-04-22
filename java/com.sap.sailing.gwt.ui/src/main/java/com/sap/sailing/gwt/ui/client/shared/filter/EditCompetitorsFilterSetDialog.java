package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class EditCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public EditCompetitorsFilterSetDialog(FilterSet<CompetitorDTO> competitorsFilterSet, List<Filter<CompetitorDTO, ?>> availableCompetitorsFilter, 
            List<String> existingFilterSetNames, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(competitorsFilterSet, availableCompetitorsFilter, existingFilterSetNames, "Edit filter", stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(competitorsFilterSet.getName());
    }
}
