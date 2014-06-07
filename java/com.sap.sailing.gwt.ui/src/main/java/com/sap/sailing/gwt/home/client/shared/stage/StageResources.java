package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface StageResources extends ClientBundle {
    public static final StageResources INSTANCE = GWT.create(StageResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/stage/Stage.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String stage();
        String stage_header();
        String stage_header_subtitle();
        String stage_header_title();
        String stage_header_message();
        String stage_band();
        String stage_band_message();
        String stage_band_name();
        String stage_band_action();
    }
}