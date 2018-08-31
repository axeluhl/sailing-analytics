package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * Used to store tags and filter sets and to apply these filters on the tags
 */
public class TagListProvider extends ListDataProvider<TagDTO> {

    private List<TagDTO> allTags;
    private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;
    private TagFilterLabel observingLabel;

    public TagListProvider() {
        allTags = new ArrayList<TagDTO>();
    }

    public void addObserveringLabel(TagFilterLabel tagFilterLabel) {
        observingLabel = tagFilterLabel;
    }

    public List<TagDTO> getAllTags() {
        return allTags;
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

    public void setCurrentFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagsFilterSet) {
        currentFilterSet = tagsFilterSet;
        if (observingLabel != null) {
            observingLabel.update(tagsFilterSet);
        }
        updateFilteredTags();
        refresh();
    }

    public int getFilteredTagsListSize() {
        return Util.size(getFilteredTags());
    }
}
