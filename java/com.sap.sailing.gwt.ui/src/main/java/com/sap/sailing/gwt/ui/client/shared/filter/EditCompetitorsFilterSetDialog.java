package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.gwt.ui.client.FilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class EditCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public EditCompetitorsFilterSetDialog(FilterSetWithUI<CompetitorDTO> competitorsFilterSet, List<FilterWithUI<CompetitorDTO, ?>> availableCompetitorsFilter, 
            List<String> existingFilterSetNames, StringMessages stringMessages, DialogCallback<FilterSetWithUI<CompetitorDTO>> callback) {
        super(competitorsFilterSet, availableCompetitorsFilter, existingFilterSetNames, stringMessages.actionEditFilter(), stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(competitorsFilterSet.getName());
    }
}
