package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class EditCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public EditCompetitorsFilterSetDialog(FilterSet<CompetitorDTO> competitorsFilterSet, StringMessages stringMessages,
            com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(competitorsFilterSet, "Edit filter", stringMessages, callback);
        
        filterListBox = createListBox(false);
        filterSetNameTextBox = createTextBox(competitorsFilterSet.getName());
    }
}
