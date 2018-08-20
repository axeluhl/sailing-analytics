package com.sap.sailing.gwt.home.shared.partials.storebadges;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiConstructor;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * {@link AbstractStoreBadge} implementation for Apple's App Store.
 * 
 * The badge images is delived from within the application. The suffix is stored in the i18n bundle and points to the
 * localized badge images. English is used for all badges, unless there is an localized (actually only german) version.
 */
public class AppStoreBadge extends AbstractStoreBadge {

    @UiConstructor
    public AppStoreBadge(String targetUrl) {
        super(targetUrl);
    }

    @Override
    protected SafeUri getBadgeImageUrl(StringMessages i18n) {
        return UriUtils.fromString("images/home/appstore" + i18n.appstoreBadgeSuffix() + ".svg");
    }

    @Override
    protected String getBadgetImageAltText(StringMessages i18n) {
        return "Get it on AppStore";
    }

}
