package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * Shows current selected filter.
 */
public class TagFilterLabel extends Label {

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;

    public TagFilterLabel(TaggingPanel taggingPanel) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = taggingPanel.getStringMessages();

        addStyleName(style.tagFilterCurrentSelection());
        update(taggingPanel.getTagListProvider().getTagFilterSet());
        taggingPanel.getTagListProvider().addObserveringLabel(this);
    }

    public void update(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        if (tagsFilterSet != null && !tagsFilterSet.getName().isEmpty()) {
            setText(stringMessages.tagCurrentFilter() + " " + tagsFilterSet.getName());
            removeStyleName(style.hidden());
        } else {
            setText("");
            addStyleName(style.hidden());
        }
        taggingPanel.refreshContentPanel();
    }
}
