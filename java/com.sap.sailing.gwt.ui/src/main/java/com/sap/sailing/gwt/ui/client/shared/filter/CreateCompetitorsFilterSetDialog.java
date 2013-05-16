package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(List<String> existingFilterSetNames, List<FilterWithUI<CompetitorDTO>> availableCompetitorFilters, StringMessages stringMessages,
            DialogCallback<FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>> callback) {
        super(new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(null), availableCompetitorFilters, existingFilterSetNames, stringMessages.actionAddFilter(), stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(null);
    }
}
