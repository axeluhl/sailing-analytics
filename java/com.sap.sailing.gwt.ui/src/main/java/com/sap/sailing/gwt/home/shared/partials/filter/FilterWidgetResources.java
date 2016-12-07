package com.sap.sailing.gwt.home.shared.partials.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface FilterWidgetResources extends ClientBundle {
    
    public static final FilterWidgetResources INSTANCE = GWT.create(FilterWidgetResources.class);

    @Source("FilterWidget.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String input_filter_container();
        String input_filter_text_input();
        String input_filter_clear_button();
    }
}
