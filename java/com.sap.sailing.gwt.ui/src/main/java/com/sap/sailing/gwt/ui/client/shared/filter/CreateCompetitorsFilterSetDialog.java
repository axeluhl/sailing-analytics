package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(List<String> existingFilterSetNames, List<String> availableCompetitorFilterNames,
            StringMessages stringMessages, DialogCallback<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> callback) {
        super(new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(null), availableCompetitorFilterNames, existingFilterSetNames, stringMessages.actionAddFilter(), stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(null);
    }
}
