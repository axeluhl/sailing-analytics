package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.FilterSet;

public class EditCompetitorsFilterSetDialog extends AbstractCompetitorsFilterSetDialog {

    public EditCompetitorsFilterSetDialog(FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>> competitorsFilterSet, List<String> availableCompetitorFilterNames, 
            List<String> existingFilterSetNames, StringMessages stringMessages, DialogCallback<FilterSet<CompetitorWithBoatDTO, FilterWithUI<CompetitorWithBoatDTO>>> callback) {
        super(competitorsFilterSet, availableCompetitorFilterNames, existingFilterSetNames, stringMessages.actionEditFilter(), stringMessages, callback);
        
        filterSetNameTextBox = createTextBox(competitorsFilterSet.getName());
    }
}
