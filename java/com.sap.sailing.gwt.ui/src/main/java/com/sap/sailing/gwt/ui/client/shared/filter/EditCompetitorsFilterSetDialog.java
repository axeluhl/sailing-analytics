package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class EditCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public EditCompetitorsFilterSetDialog(FilterSet<CompetitorDTO> competitorsFilterSet, List<String> existingFilterSetNames, StringMessages stringMessages,
            com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(competitorsFilterSet, existingFilterSetNames, "Edit filter", stringMessages, callback);
        
        filterListBox = createListBox(false);
        filterSetNameTextBox = createTextBox(competitorsFilterSet.getName());
    }
}
