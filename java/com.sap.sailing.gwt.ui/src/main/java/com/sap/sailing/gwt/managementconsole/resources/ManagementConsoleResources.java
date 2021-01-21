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

    @Source("icons/Icon-BackGLobal.svg")
    @MimeType("image/svg+xml")
    DataResource iconBack();

    @Source("icons/Icon-CloseGLobal.svg")
    @MimeType("image/svg+xml")
    DataResource iconClose();

    @Source("icons/Icon-DropdownChev.svg")
    @MimeType("image/svg+xml")
    DataResource iconDropdownChev();

    @Source("icons/Icon-EventAdd.svg")
    @MimeType("image/svg+xml")
    DataResource iconAdd();

    @Source("icons/Icon-EventFilter.svg")
    @MimeType("image/svg+xml")
    DataResource iconFilter();

    @Source("icons/Icon-Search.svg")
    @MimeType("image/svg+xml")
    DataResource iconSearch();

    @Source("icons/Icon-SearchApply.svg")
    @MimeType("image/svg+xml")
    DataResource iconApply();

    @Source("icons/Icon-SearchPrevious.svg")
    @MimeType("image/svg+xml")
    DataResource iconPrevious();

    @Source("icons/Icon-NavGlobal.svg")
    @MimeType("image/svg+xml")
    DataResource iconNavigation();

    @Source("icons/Icon-ElipseGLobal.svg")
    @MimeType("image/svg+xml")
    DataResource iconElipse();

    interface Style extends CssResource {

        String backdropImage();

        String primaryButton();

        String secondaryButton();

        String secondaryCtaButton();

        @ClassName("page-header")
        String pageHeader();

        @ClassName("flex-container")
        String flexContainer();

        @ClassName("flex-item--fixed-width")
        String flexItemFixedWidth();

        @ClassName("flex-item--auto-width")
        String flexItemAutoWidth();

        String ellipsis();
    }

    interface Icons extends CssResource {

        String icon();

        @ClassName("icon-location")
        String iconLocation();

        @ClassName("icon-back")
        String iconBack();

        @ClassName("icon-close")
        String iconClose();

        @ClassName("icon-dropdown-chev")
        String iconDropdownChev();

        @ClassName("icon-add")
        String iconAdd();

        @ClassName("icon-filter")
        String iconFilter();

        @ClassName("icon-search")
        String iconSearch();

        @ClassName("icon-apply")
        String iconApply();

        @ClassName("icon-previous")
        String iconPrevious();

        @ClassName("icon-navigation")
        String iconNavigation();

        @ClassName("icon-elipse")
        String iconElipse();
    }
}
