package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;


public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(StringMessages stringMessages,
            DialogCallback<FilterSet<CompetitorDTO>> callback) {
        super(new FilterSet<CompetitorDTO>(null), stringMessages, callback);
        
        filterListBox = createListBox(false);
        filterSetNameTextBox = createTextBox(null);
    }

}
