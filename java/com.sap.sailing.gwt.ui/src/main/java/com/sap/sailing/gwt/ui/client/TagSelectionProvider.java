package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public interface TagSelectionProvider {

    /**
     * @return a non-<code>null</code> sequence of Tags which may be empty; order tries to represent the order in
     *         which elements were selected; all Tags contained in the structure returned are also in the
     *         structure returned by {@link #getAllTags()} at the time of the call.
     */
    Iterable<TagDTO> getSelectedTags();
    
    /**
     * The intersection of {@link #getSelectedTags()} and {@link #getFilteredTags()}
     */
    Iterable<TagDTO> getSelectedFilteredTags();
    
    /**
     * @return a non-<code>null</code> sequence of all Tags which may be empty.
     */
    Iterable<TagDTO> getAllTags();

    /**
     * @return a non-<code>null</code> sequence of all Tags filtered by the applied
     * {@link #getTagsFilterSet() filter set} (which may be null).
     */
    Iterable<TagDTO> getFilteredTags();

    
    boolean isSelected(TagDTO tag);
    
    boolean hasMultiSelection();

    Color getColor(TagDTO tag);
  
    
    public FilterSet<TagDTO, Filter<TagDTO>> getTagsFilterSet();

    public void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> TagsFilterSet);

    FilterSet<TagDTO, Filter<TagDTO>> getOrCreateTagsFilterSet(String nameToAssignToNewFilterSet);
    
    /**
     * Returns <code>true</code> if the provider has any filters that will restrain the selection.
     */
    public boolean hasActiveFilters();
    
    /**
     * Removes all filters and notifies listeners about the change.
     */
    public void clearAllFilters();
    
    /**
     * @return the size of all filtered Tags
     */
    public int getFilteredTagsListSize();
}
