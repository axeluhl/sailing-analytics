package com.sap.sailing.gwt.ui.client;


import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class TagListProvider extends ListDataProvider<TagDTO> implements TagProvider{
    
    private final List<TagDTO> allTags;
    
    private FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet;     
    

    public TagListProvider(List<TagDTO> allTags, FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        this.allTags = allTags;
        this.tagsFilterSet = tagsFilterSet;
    }
    @Override
    public Iterable<TagDTO> getAllTags() {
        return allTags;
    }

    @Override
    public Iterable<TagDTO> getFilteredTags() {
        Set<TagDTO> currentFilteredList = new LinkedHashSet<>(allTags);
        
        if (tagsFilterSet != null) {
            for (Filter<TagDTO> filter : tagsFilterSet.getFilters()) {
                for (Iterator<TagDTO> i=currentFilteredList.iterator(); i.hasNext(); ) {
                    TagDTO tagDTO = i.next();
                    if (!filter.matches(tagDTO)) {
                        i.remove();
                    }
                }
            }
        }
        return currentFilteredList;
    }

    @Override
    public FilterSet<TagDTO, Filter<TagDTO>> getTagsFilterSet() {
        return tagsFilterSet;
    }

    @Override
    public void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        this.tagsFilterSet = tagsFilterSet;
        
    }

    @Override
    public FilterSet<TagDTO, Filter<TagDTO>> getOrCreateTagsFilterSet(String nameToAssignToNewFilterSet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasActiveFilters() {
        return (tagsFilterSet != null && !tagsFilterSet.getFilters().isEmpty() 
                && Util.size(getFilteredTags()) != allTags.size());
    }

    @Override
    public void clearAllFilters() {
        tagsFilterSet = null;
        
    }

    @Override
    public int getFilteredTagsListSize() {
        return Util.size(getFilteredTags());
    }
}