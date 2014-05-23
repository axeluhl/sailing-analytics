package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MainMenuResources extends ClientBundle {
    public static final MainMenuResources INSTANCE = GWT.create(MainMenuResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/MainMenu.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String sitenavigation_link();

        String sitenavigation_linksearch();

        String sitenavigationhome();

        String sitenavigation_linkhome();

        String sitenavigation_linkevents();

        String sitenavigationboatclasses();

        String sitenavigation_linkboatclasses();

        String sitenavigationsolutions();

        String sitenavigation_linksolutions();

        String sitenavigationsponsoring();

        String sitenavigation_linksponsoring();

    }
}
