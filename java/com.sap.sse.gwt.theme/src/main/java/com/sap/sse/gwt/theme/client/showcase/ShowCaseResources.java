package com.sap.sse.gwt.theme.client.showcase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ShowCaseResources extends ClientBundle {
    public static final ShowCaseResources INSTANCE = GWT.create(ShowCaseResources.class);

    @Source("showcase.gss")
    ShowcaseCss css();

    public interface ShowcaseCss extends CssResource{
        String showcaseHeader();
        String showcaseContent();
        String styleGuideHeader();
        String styleGuideContent();
        
        String styleGuideRow();
        String styleGuideRowDescription();
        String styleGuideRowDescriptionTitle();
        String styleGuideRowContent();
        String styleGuideRowCode();
        String styleGuideRowDescription_codeLink();
        
        // TODO: these style should come from the main.css
        String styleGuideDetailsTypeface_semibold();
        String styleGuideDetailsTypeface_bold();
        String styleGuideDetailsTypeface_regular();
   }
}