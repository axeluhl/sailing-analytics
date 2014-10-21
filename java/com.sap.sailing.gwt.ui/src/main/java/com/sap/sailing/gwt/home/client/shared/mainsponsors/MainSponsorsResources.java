package com.sap.sailing.gwt.home.client.shared.mainsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MainSponsorsResources extends ClientBundle {
    public static final MainSponsorsResources INSTANCE = GWT.create(MainSponsorsResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/mainsponsors/MainSponsors.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String mainsponsors();
    }
    
//    @Source("com/sap/sailing/gwt/home/images/xxx.png")
//    ImageResource sponsorTeaserImage();
}
