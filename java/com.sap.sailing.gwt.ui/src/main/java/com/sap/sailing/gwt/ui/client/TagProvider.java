package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public interface TagProvider {

    /**
     * @return all tags.
     */
    List<TagDTO> getAllTags();

    /**
     * @return all tags which match current filtering criteria
     */
    List<TagDTO> getFilteredTags();
  
    
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
