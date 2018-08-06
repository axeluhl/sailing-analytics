package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TagSelectionProvider;
import com.sap.sailing.gwt.ui.shared.TagDTO;

public class SelectedTagsFilter implements FilterWithUI<TagDTO>, TagSelectionProviderFilterContext {
    public static final String FILTER_NAME = "SelectedTagsFilter";

    private TagSelectionProvider tagsSelectionProvider;
    
    public SelectedTagsFilter() {
        tagsSelectionProvider = null;
    }

    @Override
    public boolean matches(TagDTO tag) {
        boolean result = false;
        if(tagsSelectionProvider != null && tagsSelectionProvider.isSelected(tag)) {
            result = true;
        }
        return result;
    }
    
    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.tagSelectedTags();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return stringMessages.tagSelectedTags();
    }
    
    @Override
    public TagSelectionProvider getTagSelectionProvider() {
        return tagsSelectionProvider;
    }

    @Override
    public void setTagSelectionProvider(TagSelectionProvider tagSelectionProvider) {
        this.tagsSelectionProvider = tagSelectionProvider;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        return null;
    }

    @Override
    public FilterUIFactory<TagDTO> createUIFactory() {
        return null;
    }

    @Override
    public FilterWithUI<TagDTO> copy() {
        return new SelectedTagsFilter();
    }
}
