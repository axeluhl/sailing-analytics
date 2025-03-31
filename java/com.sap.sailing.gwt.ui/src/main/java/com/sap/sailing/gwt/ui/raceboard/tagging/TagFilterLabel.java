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

    private final TaggingComponent taggingComponent;
    private final StringMessages stringMessages;

    /**
     * Creates {@link Label} and adds it as observer at the {@link TagListProvider}.
     * 
     * @param taggingComponent
     *            provides reference to {@link TagListProvider} and {@link StringMessages}.
     */
    public TagFilterLabel(TaggingComponent taggingComponent, StringMessages stringMessages) {
        this.taggingComponent = taggingComponent;
        this.stringMessages = stringMessages;
        addStyleName(style.tagFilterCurrentSelection());
        update(taggingComponent.getTagListProvider().getTagFilterSet());
        taggingComponent.getTagListProvider().addObserveringLabel(this);
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
        taggingComponent.refreshContentPanel();
    }
}
