package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
/**
 * 
 * @author D067890
 * Used to store tags and tagFilterSets
 *
 */
public interface TagProvider {

    /**
     * adds new tags, call {@link #updateFilteredTags() updateFilteredTags} afterwards so list of filtered tags can contain new tags
     * @param list of tags which shall be added
     */
    void addTags(List<TagDTO> tags);
    
    
    /**
     * adds a new tag, call {@link #updateFilteredTags() updateFilteredTags} afterwards so list of filtered tag can contain new tags
     * @param tag which shall be added
     */
    void addTag(TagDTO tag);

    /**
     * @return all filtered tags
     */
    List<TagDTO> getFilteredTags();

    int getFilteredTagsListSize();

    void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet);

    FilterSet<TagDTO, Filter<TagDTO>> getTagsFilterSet();  
    
    /**
     * filter all tags by tagsFilterSet
     */
    void updateFilteredTags();

    /**
     * @return all tags without filtering
     */
    List<TagDTO> getAllTags();
}