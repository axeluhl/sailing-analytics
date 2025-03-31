package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RaceviewerLaunchPadResources extends ClientBundle {
    public static final RaceviewerLaunchPadResources INSTANCE = GWT.create(RaceviewerLaunchPadResources.class);

    @Source("RaceviewerLaunchPad.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String raceviewerlaunchpad();
        String raceviewerlaunchpadwithname();
        String raceviewerlaunchpad_wrapper();
        String raceviewerlaunchpad_name();
        String raceviewerlaunchpad_date();
        String raceviewerlaunchpad_cta();
        String raceviewerlaunchpadlive();
        String raceviewerlaunchpadplanned();
        String raceviewerlaunchpad_content_item();
        String raceviewerlaunchpad_content_item_icon();
        String raceviewerlaunchpad_icon();
        String raceviewerlaunchpad_content();
        String raceviewerlaunchpad_content_item_title();
        String raceviewerlaunchpad_not_tracked();
    }
}
