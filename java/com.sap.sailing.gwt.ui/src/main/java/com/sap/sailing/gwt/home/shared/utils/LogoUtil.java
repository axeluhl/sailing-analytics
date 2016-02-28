package com.sap.sailing.gwt.home.shared.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sap.sailing.gwt.home.communication.event.HasLogo;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

/**
 * Utility class to set logo url on UI elements using a default logo as fallback if no logo is provided.
 */
public abstract class LogoUtil {

    private static final SafeUri DEFAULT_EVENT_LOGO = SharedHomeResources.INSTANCE.defaultEventLogoImage().getSafeUri();

    /**
     * Sets the logo provided by the given {@link EventViewDTO} (or default logo if <code>null</code>) as background
     * image and the {@link EventViewDTO#getDisplayName() events display name} as title of the given UI element.
     * 
     * @param eventLogoUi {@link Element} where the logo is set
     * @param event {@link EventViewDTO} providing data
     */
    public static void setEventLogo(Element eventLogoUi, HasLogo event) {
        String logoUrl = event.getLogoImage() != null ? event.getLogoImage().getSourceRef() : DEFAULT_EVENT_LOGO.asString();
        setLogoAndTitle(eventLogoUi, logoUrl, event.getDisplayName());
    }

    private static void setLogoAndTitle(Element logoUi, String logoUrl, String logoTitle) {
        logoUi.getStyle().setBackgroundImage("url(" + logoUrl + ")");
        logoUi.setTitle(logoTitle);
    }

}
