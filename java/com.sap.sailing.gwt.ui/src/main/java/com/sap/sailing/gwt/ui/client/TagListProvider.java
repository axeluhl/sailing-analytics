package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class TagListProvider extends ListDataProvider<TagDTO> implements TagProvider {

    private List<TagDTO> allTags = new ArrayList<TagDTO>();

    private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;

    public TagListProvider(FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet) {
        this.currentFilterSet = currentFilterSet;
    }

    @Override
    public List<TagDTO> getAllTags() {
        return allTags;
    }

    @Override
    public void addTags(final List<TagDTO> tags) {
        if (tags != null) {
            for (TagDTO tag : tags) {
                addTag(tag);
            }
        }
    }

    @Override
    public void addTag(final TagDTO tag) {
        if (tag != null) {
            allTags.add(tag);
        }
    }

    @Override
    public List<TagDTO> getFilteredTags() {
        return getList();
    }

    @Override
    public void updateFilteredTags() {
        List<TagDTO> currentFilteredList = new ArrayList<TagDTO>(getAllTags());

        if (currentFilterSet != null) {
            for (TagDTO tag : currentFilteredList) {
                for (Filter<TagDTO> filter : currentFilterSet.getFilters()) {
                    if (!filter.matches(tag)) {
                        currentFilteredList.remove(tag);
                        break;
                    }
                }
            }
        }
        setList(currentFilteredList);
    }

    @Override
    public FilterSet<TagDTO, Filter<TagDTO>> getTagsFilterSet() {
        return currentFilterSet;
    }

    @Override
    public void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        this.currentFilterSet = tagsFilterSet;

    }

    @Override
    public boolean hasActiveFilters() {
        return (currentFilterSet != null && !currentFilterSet.getFilters().isEmpty()
                && Util.size(getFilteredTags()) != getAllTags().size());
    }

    @Override
    public void clearAllFilters() {
        currentFilterSet = null;

    }

    @Override
    public int getFilteredTagsListSize() {
        return Util.size(getFilteredTags());
    }
}