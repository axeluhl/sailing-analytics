package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class TagListProvider extends ListDataProvider<TagDTO> implements TagProvider {

    private List<TagDTO> allTags = new ArrayList<TagDTO>();    
    private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;     

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
            for (Filter<TagDTO> filter : currentFilterSet.getFilters()) {
                for (Iterator<TagDTO> i = currentFilteredList.iterator(); i.hasNext();) {
                    TagDTO tag = i.next();
                    if (!filter.matches(tag)) {
                        i.remove();
                    }
                }
            }
        }
        currentFilteredList.sort(new Comparator<TagDTO>() {
            @Override
            public int compare(TagDTO tag1, TagDTO tag2) {
                long time1 = tag1.getRaceTimepoint().asMillis();
                long time2 = tag2.getRaceTimepoint().asMillis();
                return time1 < time2 ? -1 : time1 == time2 ? 0 : 1;
            }
        });
        setList(currentFilteredList);
    }

    @Override
    public FilterSet<TagDTO, Filter<TagDTO>> getTagsFilterSet() {
        return currentFilterSet;
    }

    @Override
    public void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        this.currentFilterSet = tagsFilterSet;
        this.updateFilteredTags();
        this.refresh();
    }

    @Override
    public int getFilteredTagsListSize() {
        return Util.size(getFilteredTags());
    }
}