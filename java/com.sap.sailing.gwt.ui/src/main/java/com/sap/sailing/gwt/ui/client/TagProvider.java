package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public interface TagProvider {

    void addTags(List<TagDTO> tags);

    void addTag(TagDTO tag);

    List<TagDTO> getFilteredTags();

    int getFilteredTagsListSize();

    void clearAllFilters();

    boolean hasActiveFilters();

    void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet);

    FilterSet<TagDTO, Filter<TagDTO>> getTagsFilterSet();

    void updateFilteredTags();

    List<TagDTO> getAllTags();


}
