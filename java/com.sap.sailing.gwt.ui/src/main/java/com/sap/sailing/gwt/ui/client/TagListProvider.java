package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * @author Julian Rendl(D067890) Used to store tags and filter sets and to apply these filters on the tags
 */
public class TagListProvider extends ListDataProvider<TagDTO> {

    private List<TagDTO> allTags = new ArrayList<TagDTO>();
    private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;

    public TagListProvider() {
    }

    public List<TagDTO> getAllTags() {
        return allTags;
    }

    /**
     * adds new tags, call {@link #updateFilteredTags() updateFilteredTags} afterwards so list of filtered tags can
     * contain new tags
     * 
     * @param list
     *            of tags which shall be added
     */
    public void addTags(final List<TagDTO> tags) {
        if (tags != null) {
            for (TagDTO tag : tags) {
                addTag(tag);
            }
        }
    }

    /**
     * adds a new tag, call {@link #updateFilteredTags() updateFilteredTags} afterwards so list of filtered tag can
     * contain new tags
     * 
     * @param tag
     *            which shall be added
     */
    public void addTag(final TagDTO tag) {
        if (tag != null) {
            allTags.add(tag);
        }
    }

    public List<TagDTO> getFilteredTags() {
        return getList();
    }

    /**
     * filter all tags by tagsFilterSet
     */
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

    public FilterSet<TagDTO, Filter<TagDTO>> getTagFilterSet() {
        return currentFilterSet;
    }

    public void setTagsFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        currentFilterSet = tagsFilterSet;
        updateFilteredTags();
        refresh();
    }

    public int getFilteredTagsListSize() {
        return Util.size(getFilteredTags());
    }
}