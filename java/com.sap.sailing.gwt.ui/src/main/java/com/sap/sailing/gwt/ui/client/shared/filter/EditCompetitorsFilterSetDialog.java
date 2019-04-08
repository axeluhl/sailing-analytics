package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.FilterSet;

public class EditCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public EditCompetitorsFilterSetDialog(FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> competitorsFilterSet, List<String> availableCompetitorFilterNames, 
            List<String> existingFilterSetNames, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> callback) {
        super(competitorsFilterSet, availableCompetitorFilterNames, existingFilterSetNames, stringMessages.actionEditFilter(), stringMessages, callback);
        filterSetNameTextBox = createTextBox(competitorsFilterSet.getName());
    }
}
