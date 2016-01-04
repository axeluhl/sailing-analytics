package com.sap.sailing.gwt.home.desktop.partials.raceoffice;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RaceOfficeSectionResources extends ClientBundle {
    
    public static final RaceOfficeSectionResources INSTANCE = GWT.create(RaceOfficeSectionResources.class);
    
    @Source("RaceOfficeSection.gss")
    LocalCss css();
    
    public interface LocalCss extends CssResource {
        String raceoffice();
        String raceoffice_title();
        String raceoffice_links();
        String raceoffice_link();
    }

}
