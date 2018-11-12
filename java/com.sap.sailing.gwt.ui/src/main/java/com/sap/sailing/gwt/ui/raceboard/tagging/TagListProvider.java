package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * Provides list of {@link TagDTO tags} sorted by race timepoint and is able to filter theses {@link TagDTO tags} with
 * the current selected {@link FilterSet}.
 */
public class TagListProvider extends ListDataProvider<TagDTO> {

    private FilterSet<TagDTO, Filter<TagDTO>> currentFilterSet;
    /**
     * Stores {@link TagDTO tags} sorted by their {@link TagDTO#getRaceTimepoint() race timepoint}.
     */
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
     * Adds {@link TagDTO tag} to {@link #getAllTags() list of all tags}.
     * 
     * @param tag
     *            tag to be added
     */
    protected void add(TagDTO tag) {
        // tags are sorted by race timepoint
        for (int i = 0; i < allTags.size(); i++) {
            if (allTags.get(i).getRaceTimepoint().after(tag.getRaceTimepoint())) {
                allTags.add(i, tag);
                return;
            }
        }
        // add tag to end in case there is no newer tag
        allTags.add(tag);
    }

    /**
     * Adds list of {@link TagDTO tags} to {@link #getAllTags() list of all tags}.
     * 
     * @param tags
     *            tags to be added
     */
    protected void addAll(List<TagDTO> tags) {
        for (TagDTO tag : tags) {
            add(tag);
        }
    }

    /**
     * Removes {@link TagDTO tag} from {@link #getAllTags() list of all tags}.
     * 
     * @param tag
     *            tag to be removed
     */
    protected void remove(TagDTO tag) {
        allTags.remove(tag);
    }

    /**
     * Removes all private {@link TagDTO tags} from {@link #getAllTags() list of all tags}.
     */
    protected void removePrivateTags() {
        allTags.removeIf(tag -> !tag.isVisibleForPublic());
    }

    /**
     * Clears {@link #getAllTags() list of all tags}.
     */
    protected void clear() {
        allTags.clear();
    }

    /**
     * Returns unmodifiable list of all {@link TagDTO tags} ignoring the {@link #currentFilterSet current filter set}.
     * 
     * @return list of all {@link TagDTO tags}
     */
    protected List<TagDTO> getAllTags() {
        return Collections.unmodifiableList(allTags);
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

        currentFilteredList.sort((tag1, tag2) -> {
            TimePoint tag1Time = tag1.getRaceTimepoint();
            TimePoint tag2Time = tag2.getRaceTimepoint();
            if (tag1Time.before(tag2Time)) {
                return -1;
            } else if (tag1Time.after(tag2Time)) {
                return 1;
            } else {
                return 0;
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
