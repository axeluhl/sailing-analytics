package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.TagDTO;

/**
 * A factory for FilterWithUI<Tag> instances
 */
public class TagFilterWithUIFactory {
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
