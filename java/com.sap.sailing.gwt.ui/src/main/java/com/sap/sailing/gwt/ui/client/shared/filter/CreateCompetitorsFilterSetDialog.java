package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(List<String> existingFilterSetNames, List<FilterWithUI<CompetitorDTO, ?>> availableCompetitorsFilter, StringMessages stringMessages,
            DialogCallback<FilterSetWithUI<CompetitorDTO>> callback) {
        super(new FilterSetWithUI<CompetitorDTO>(null), availableCompetitorsFilter, existingFilterSetNames, "Create a filter", stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(null);
    }
}
