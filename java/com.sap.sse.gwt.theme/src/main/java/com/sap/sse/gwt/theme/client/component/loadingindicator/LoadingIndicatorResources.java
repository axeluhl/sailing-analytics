package com.sap.sse.gwt.theme.client.component.loadingindicator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface LoadingIndicatorResources extends ClientBundle {
    public static final LoadingIndicatorResources INSTANCE = GWT.create(LoadingIndicatorResources.class);

    @Source("LoadingIndicator.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String loadingIndicator();
    }
}
