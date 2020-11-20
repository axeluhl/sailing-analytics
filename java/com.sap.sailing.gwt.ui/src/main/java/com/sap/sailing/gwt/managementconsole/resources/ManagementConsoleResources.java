package com.sap.sailing.gwt.managementconsole.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.client.ImageResource;

public interface ManagementConsoleResources extends ClientBundle {

    String COLORS = "com/sap/sailing/gwt/managementconsole/resources/ManagementConsoleColors.gss";
    String ICONS = "com/sap/sailing/gwt/managementconsole/resources/ManagementConsoleIcons.gss";
    String STYLES = "com/sap/sailing/gwt/managementconsole/resources/ManagementConsoleStyles.gss";

    ManagementConsoleResources INSTANCE = GWT.create(ManagementConsoleResources.class);

    @Source({ COLORS, STYLES })
    Style style();

    @Source(ICONS)
    Icons icons();

    @Source("images/Image-BackdropGeneral.png")
    ImageResource backdropGeneral();

    @Source("images/Image-BackdropGeneralSized.png")
    ImageResource backdropGeneralSized();

    @Source("icons/Icon-EventListLocation.svg")
    @MimeType("image/svg+xml")
    DataResource iconLocation();

    @Source("icons/Icon-NavGlobal.svg")
    @MimeType("image/svg+xml")
    DataResource iconNavigation();

    interface Style extends CssResource {

        String primaryButton();

        String secondaryButton();

        String secondaryCtaButton();

    }

    interface Icons extends CssResource {

        String icon();

        @ClassName("icon-location")
        String iconLocation();

        @ClassName("icon-navigation")
        String iconNavigation();

    }
}
