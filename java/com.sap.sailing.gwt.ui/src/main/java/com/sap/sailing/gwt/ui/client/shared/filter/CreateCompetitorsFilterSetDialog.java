package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(List<String> existingFilterSetNames, StringMessages stringMessages,
            DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(new FilterSet<CompetitorDTO>(null), existingFilterSetNames, "Create a filter", stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(null);
    }

}
