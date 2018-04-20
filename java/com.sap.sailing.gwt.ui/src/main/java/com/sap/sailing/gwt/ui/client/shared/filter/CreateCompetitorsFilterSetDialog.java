package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.FilterSet;

public class CreateCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public CreateCompetitorsFilterSetDialog(List<String> existingFilterSetNames, List<String> availableCompetitorFilterNames,
            StringMessages stringMessages, DialogCallback<FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>>> callback) {
        super(new FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>>(null), availableCompetitorFilterNames, existingFilterSetNames, stringMessages.actionAddFilter(), stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(null);
    }
}
