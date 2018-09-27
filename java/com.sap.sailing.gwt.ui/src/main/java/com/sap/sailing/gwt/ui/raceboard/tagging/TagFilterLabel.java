package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * Shows current selected filter used at {@link TagFilterPanel}. Observes {@link TagListProvider} for changes of the
 * selected filter.
 */
public class TagFilterLabel extends Label {

    private final TagPanelStyle style = TaggingPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final StringMessages stringMessages;

    /**
     * Creates {@link Label} and adds it as observer at the {@link TagListProvider}.
     * 
     * @param taggingPanel
     *            provides reference to {@link TagListProvider} and {@link StringMessages}.
     */
    public TagFilterLabel(TaggingPanel taggingPanel, StringMessages stringMessages) {
        this.taggingPanel = taggingPanel;
        this.stringMessages = stringMessages;

        addStyleName(style.tagFilterCurrentSelection());
        update(taggingPanel.getTagListProvider().getTagFilterSet());
        taggingPanel.getTagListProvider().addObserveringLabel(this);
    }

    /**
     * Updates label text to match given <code>tagFilterSet</code>. Hides label if no filter is applied, otherwise label
     * is shown.
     * 
     * @param tagFilterSet
     *            current selected filter
     */
    public void update(FilterSet<TagDTO, Filter<TagDTO>> tagFilterSet) {
        if (tagFilterSet != null && tagFilterSet.getName() != null && !tagFilterSet.getName().isEmpty()) {
            setText(stringMessages.tagCurrentFilter() + " " + tagFilterSet.getName());
            removeStyleName(style.hidden());
        } else {
            setText("");
            addStyleName(style.hidden());
        }
        taggingPanel.refreshContentPanel();
    }
}
