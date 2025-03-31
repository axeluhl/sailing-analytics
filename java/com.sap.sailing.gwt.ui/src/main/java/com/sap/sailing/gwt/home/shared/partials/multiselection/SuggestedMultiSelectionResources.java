package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SuggestedMultiSelectionResources extends ClientBundle {
    
    public static final SuggestedMultiSelectionResources INSTANCE = GWT.create(SuggestedMultiSelectionResources.class);

    @Source("SuggestedMultiSelection.gss")
    LocalCss css();
    
    public interface LocalCss extends CssResource {
        String suggestions();
        String suggestionsMobile();
        String suggestionsHeader();
        String suggestionsHeaderTitle();
        String suggestionsContent();
        String suggestionsSettingsMenuItem();
        String suggestionsSettingsMenuItemLabel();
        String suggestionsToggleButton();
        String suggestionsContentToolbar();
        String suggestionsAddFilter();
        String suggestionsRemoveButton();
        String suggestionsContentTable();
        String suggestionsSelectedItem();
        String suggestionsItemDescription();
        String suggestionsItemDescriptionImage();
        String suggestionsItemDescriptionId();
        String suggestionsContentSeparator();
    }
}
