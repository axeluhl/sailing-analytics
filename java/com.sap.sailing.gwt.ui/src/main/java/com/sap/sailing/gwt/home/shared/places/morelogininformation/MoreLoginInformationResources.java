package com.sap.sailing.gwt.home.shared.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface MoreLoginInformationResources extends ClientBundle {

    public static final MoreLoginInformationResources INSTANCE = GWT.create(MoreLoginInformationResources.class);

    ImageResource simulator();

    ImageResource notifications();
    
    ImageResource settings();
}
