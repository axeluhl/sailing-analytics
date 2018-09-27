package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;

public final class BoatclassElementBuilder {

    public static Element generateBoatclassElementForMobile(final BoatClassDTO boatclass) {
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
        Element elem = DOM.createDiv();
        elem.setInnerSafeHtml(SharedSailorProfileResources.TEMPLATES.buildBoatclassIconWithName(
                BoatClassImageResolver.getBoatClassIconResource(boatclass.getName()).getSafeUri().asString(),
                boatclass.getName()));
        elem.addClassName(SailorProfileMobileResources.INSTANCE.css().boatclassWithNameEntry());
        return elem;
    }
}
