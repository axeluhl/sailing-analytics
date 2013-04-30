package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.filter.FilterSet;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(List<String> existingFilterSetNames, List<ValueFilterWithUI<CompetitorDTO, ?>> availableCompetitorsFilter, StringMessages stringMessages,
            DialogCallback<FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>> callback) {
        super(new FilterSet<CompetitorDTO, ValueFilterWithUI<CompetitorDTO, ?>>(null), availableCompetitorsFilter, existingFilterSetNames, stringMessages.actionAddFilter(), stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(null);
    }
}
