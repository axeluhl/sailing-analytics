package com.sap.sailing.gwt.ui.client;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class TagListProvider extends ListDataProvider<TagDTO> implements TagProvider{
    
    private FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet;     
    

    public TagListProvider(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        this.tagsFilterSet = tagsFilterSet;
    }
    
    @Override
    public List<TagDTO> getAllTags() {
        return getList();
    }

    @Override
    public List<TagDTO> getFilteredTags() {
        List<TagDTO> currentFilteredList = new ArrayList<TagDTO>(getAllTags());
        
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
                && Util.size(getFilteredTags()) != getAllTags().size());
    }

    @Override
    public void clearAllFilters() {
        tagsFilterSet = null;
        
    }

    @Override
    public int getFilteredTagsListSize() {
        return Util.size(getFilteredTags());
    }

    public void addTag(TagDTO tag) {
        getAllTags().add(tag);
    }
}