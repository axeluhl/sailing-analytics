package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.List;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.filter.FilterSet;

public class CreateTagsFilterSetDialog extends AbstractTagsFilterSetDialog {

    public CreateTagsFilterSetDialog(List<String> existingFilterSetNames, List<String> availableTagFilterNames,
            StringMessages stringMessages, DialogCallback<FilterSet<TagDTO, FilterWithUI<TagDTO>>> callback) {
        super(new FilterSet<TagDTO, FilterWithUI<TagDTO>>(null), availableTagFilterNames, existingFilterSetNames, stringMessages.tagEditFilter(), stringMessages, callback);
        filterSetNameTextBox = createTextBox(null);
    }
}
