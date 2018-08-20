package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegattaStatusResources extends ClientBundle {
    public static final RegattaStatusResources INSTANCE = GWT.create(RegattaStatusResources.class);

    @Source("RegattaStatus.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String regattastatus();
        String grid();
        String regattastatus_header();
        String regattastatus_header_title();
        String regattastatus_content();
        String regattastatus_content_regatta();
        String regattastatus_content_regatta_race();
        String regattastatus_content_regatta_race_title();
        String regattastatus_content_regatta_race_flag();
        String regattastatus_content_regatta_race_state();
        String regattastatus_content_regatta_race_legs();
        String regattastatus_content_regatta_race_legs_progressbar();
        String regattastatus_content_regatta_race_legs_progressbar_progress();
        String regattastatus_content_regatta_race_arrow();
        String sectionheader();
        String togglecontainerhidden();
    }
}
