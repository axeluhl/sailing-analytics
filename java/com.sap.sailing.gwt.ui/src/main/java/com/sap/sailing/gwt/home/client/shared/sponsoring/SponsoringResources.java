package com.sap.sailing.gwt.home.client.shared.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.sap.sailing.gwt.home.client.shared.solutions.SolutionsResources.ImportedMainCss;
import com.sap.sailing.gwt.home.client.shared.sponsoring.GlobalResources.GlobalCss;

public interface SponsoringResources extends ClientBundle {
    public static final SponsoringResources INSTANCE = GWT.create(SponsoringResources.class);

    @ImportedWithPrefix("global")
    interface ImportedGlobalCss extends GlobalCss {
    }

    @Import(value = {ImportedGlobalCss.class})
    @Source("com/sap/sailing/gwt/home/client/shared/sponsoring/Sponsoring.css")
    LocalCss css();

    
    public interface MinWidth25emCss extends LocalCss {
    }

    @Import(value = {ImportedMainCss.class})
    @Source("com/sap/sailing/gwt/home/client/shared/sponsoring/SponsoringMedium.css")
    MinWidth25emCss mediumCss();

    public interface MinWidth50emCss extends LocalCss {
    }

    @Import(value = {ImportedMainCss.class})
    @Source("com/sap/sailing/gwt/home/client/shared/sponsoring/SponsoringLarge.css")
    MinWidth50emCss largeCss();

    
    public interface LocalCss extends CssResource {
        String localA();
    }
}
