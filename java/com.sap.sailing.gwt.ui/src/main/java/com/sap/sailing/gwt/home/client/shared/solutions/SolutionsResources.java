package com.sap.sailing.gwt.home.client.shared.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.resources.client.CssResource.Shared;
import com.sap.sailing.gwt.home.client.HomeResources.MainCss;

public interface SolutionsResources extends ClientBundle {
    public static final SolutionsResources INSTANCE = GWT.create(SolutionsResources.class);

    @ImportedWithPrefix("global")
    interface ImportedMainCss extends MainCss {
    }

    @Import(value = {ImportedMainCss.class})
    @Source("com/sap/sailing/gwt/home/client/shared/solutions/Solutions.css")
    LocalCss css();

    public interface MinWidth25emCss extends LocalCss {
    }

    @Import(value = {ImportedMainCss.class})
    @Source("com/sap/sailing/gwt/home/client/shared/solutions/SolutionsMedium.css")
    MinWidth25emCss mediumCss();

    public interface MinWidth50emCss extends LocalCss {
    }

    @Import(value = {ImportedMainCss.class})
    @Source("com/sap/sailing/gwt/home/client/shared/solutions/SolutionsLarge.css")
    MinWidth50emCss largeCss();

    @Shared
    public interface LocalCss extends CssResource {
        String solutions();
        String solutions_nav();
        String solutions_nav_link();
        String solutions_content();
        String parallax();
        String solutions_contentsap();
        String solutions_contentrace();
        String background();
        String noparallax();
        String solutions_contentpost();
        String solutions_contenttraining();
        String solutions_contentsimulator();
        String solutions_content_linkappstore();
    }
}
