package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface SharedSailorProfileResources extends ClientBundle {

    public static final SharedSailorProfileResources INSTANCE = GWT.create(SharedSailorProfileResources.class);

    public static final SailorProfileTemplates TEMPLATES = GWT.create(SailorProfileTemplates.class);

    public static final String TRUSTED_BUILD_BOAT_CLASS_ICON_STYLE_STRING = "height: 40px; width: 40px; margin-left: 5px; "
            + "display: inline-block; background-size: cover; background-repeat: no-repeat; background-position: center;";

    public static final String TRUSTED_BUILD_BOAT_CLASS_ICON_WITH_NAME_STYLE_STRING = "height: 30px; width: 30px; "
            + "display: inline-block; background-size: cover; background-repeat: no-repeat; "
            + "background-position: center; vertical-align:middle;";

    @Source("SailorProfiles.gss")
    SailorProfilesCss css();

    public interface SailorProfileTemplates extends SafeHtmlTemplates {
        @Template("<div style=\"{0}\"></div>")
        SafeHtml buildBoatclassIcon(SafeStyles styles);

        @Template("<div style=\"{0}\"></div><span style=\"vertical-align: middle;font-size: 0.75em;margin-left: 5px;\">{1}</span>")
        SafeHtml buildBoatclassIconWithName(SafeStyles styles, String boatClassName);
    }

    public interface SailorProfilesCss extends CssResource {
        String rotateLeft();

        String competitorWithoutClubnameItemDescription();

        String inverseButton();
    }
}
