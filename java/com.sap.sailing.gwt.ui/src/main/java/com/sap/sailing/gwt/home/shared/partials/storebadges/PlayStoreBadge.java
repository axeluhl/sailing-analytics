package com.sap.sailing.gwt.home.shared.partials.storebadges;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiConstructor;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * {@link AbstractStoreBadge} implementation for Google's Play Store / Google Play.
 * 
 * The badge link is stored in the i18n bundle. English is used for all badges, unless there is an localized (actually
 * only german) version.
 * 
 */
public class PlayStoreBadge extends AbstractStoreBadge {

    @UiConstructor
    public PlayStoreBadge(String targetUrl) {
        super(targetUrl);
    }

    @Override
    protected SafeUri getBadgeImageUrl(StringMessages i18n) {
        return UriUtils.fromString(i18n.playstoreBadge());
    }

    @Override
    protected String getBadgetImageAltText(StringMessages i18n) {
        return "Get it on Google Play";
    }

}
