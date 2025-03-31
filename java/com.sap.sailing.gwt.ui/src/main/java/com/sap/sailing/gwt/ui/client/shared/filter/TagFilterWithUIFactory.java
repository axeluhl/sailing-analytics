package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.TagDTO;

/**
 * A factory for FilterWithUI<TagDTO> instances
 */
public class TagFilterWithUIFactory {
    /**
     * Creates and returns a requested {@link FilterWithUI}
     * @param filterName The name of the requested filter 
     * @return A new Instance of the requested {@link FilterWithUI}
     */
    public static FilterWithUI<TagDTO> createFilter(String filterName) {
        if (TagUsernameFilter.FILTER_NAME.equals(filterName)) {
            return new TagUsernameFilter();
        } else if (TagTagFilter.FILTER_NAME.equals(filterName)) {
            return new TagTagFilter();
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
