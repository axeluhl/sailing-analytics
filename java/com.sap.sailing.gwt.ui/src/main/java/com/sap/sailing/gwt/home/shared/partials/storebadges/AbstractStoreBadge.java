package com.sap.sailing.gwt.home.shared.partials.storebadges;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Abstract base class for store badges, showing a specific image while linking to a provided URL.
 */
abstract class AbstractStoreBadge extends Widget {

    private static AbstractStoreBadgeUiBinder uiBinder = GWT.create(AbstractStoreBadgeUiBinder.class);

    interface AbstractStoreBadgeUiBinder extends UiBinder<Element, AbstractStoreBadge> {
    }

    @UiField
    StringMessages i18n;
    @UiField
    AnchorElement badgeLinkUi;
    @UiField
    ImageElement badgeImageUi;

    /**
     * Creates a new {@link AbstractStoreBadge} instance with links to the provided URL.
     * 
     * @param targetUrl
     *            the {@link String URL} to link to
     */
    protected AbstractStoreBadge(String targetUrl) {
        setElement(uiBinder.createAndBindUi(this));
        this.badgeLinkUi.setHref(UriUtils.fromString(targetUrl));
        this.badgeImageUi.setSrc(getBadgeImageUrl(i18n).asString());
        this.badgeImageUi.setAlt(getBadgetImageAltText(i18n));
    }

    /**
     * @param i18n
     *            the {@link StringMessages} which can be used to internationalize the image URI
     * @return the {@link SafeUri URI} for the badge image
     */
    protected abstract SafeUri getBadgeImageUrl(StringMessages i18n);

    /**
     * @param i18n
     *            the {@link StringMessages} which can be used to internationalize the image alternative text
     * @return the {@link String alternative text} for the badge image
     */
    protected abstract String getBadgetImageAltText(StringMessages i18n);

}
