package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * Provides list of {@link TagDTO tags} and is able to filter theses {@link TagDTO tags} with the current selected
 * {@link FilterSet}.
 */
public class TagListProvider extends ListDataProvider<TagDTO> {

    private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;
    private final List<TagDTO> allTags;
    private final List<TagFilterLabel> observingLabels;

    /**
     * Creates instance without tags or filters.
     */
    protected TagListProvider() {
        allTags = new ArrayList<TagDTO>();
        observingLabels = new ArrayList<TagFilterLabel>();
    }

    /**
     * Used by {@link TagFilterLabel} to inform {@link TagFilterLabel} about changes of the {@link #currentFilterSet
     * current filter set}.
     * 
     * @param tagFilterLabel
     */
    protected void addObserveringLabel(TagFilterLabel tagFilterLabel) {
        observingLabels.add(tagFilterLabel);
    }

    /**
     * Informs {@link #observingLabels observing labels} about the changed {@link #currentFilterSet selected filter
     * set}.
     */
    private void updateOberservingLabels() {
        for (TagFilterLabel label : observingLabels) {
            label.update(currentFilterSet);
        }
    }

    /**
     * Returns list of all {@link TagDTO tags} ignoring the {@link #currentFilterSet current filter set}.
     * 
     * @return list of all {@link TagDTO tags}
     */
    protected List<TagDTO> getAllTags() {
        return allTags;
    }

    /**
     * Returns list of all {@link TagDTO tags} which are filtered by {@link #currentFilterSet current filter set}.
     * 
     * @return list of filtered {@link TagDTO tags}
     */
    protected List<TagDTO> getFilteredTags() {
        return getList();
    }

    /**
     * Updates {@link #getList() list} of filtered {@link TagDTO tags} by applying {@link #currentFilterSet}.
     */
    protected void updateFilteredTags() {
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

    /**
     * Returns {@link #currentFilterSet current filter set}.
     * 
     * @return current selected filter set
     */
    protected FilterSet<TagDTO, Filter<TagDTO>> getTagFilterSet() {
        return currentFilterSet;
    }

    /**
     * Sets {@link #currentFilterSet current selected filter set} to given <code>tagFilterSet</code> and informs all
     * observing labels.
     * 
     * @param tagFilterSet
     *            selected filter set
     */
    protected void setCurrentFilterSet(FilterSet<TagDTO, Filter<TagDTO>> tagFilterSet) {
        currentFilterSet = tagFilterSet;
        updateFilteredTags();
        updateOberservingLabels();
        refresh();
    }
}
