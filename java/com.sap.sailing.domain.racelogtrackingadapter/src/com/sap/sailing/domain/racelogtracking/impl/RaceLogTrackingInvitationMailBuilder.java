package com.sap.sailing.domain.racelogtracking.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.mail.QRCodeMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableDefaultMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMultipartSupplier;
import com.sap.sse.shared.media.ImageDescriptor;

class RaceLogTrackingInvitationMailBuilder {

    /**
     * URL prefix, so the iOS app will recognize as deep link and pass anything after it to the app for analysis.
     */
    private static final String IOS_DEEP_LINK_PREFIX = "comsapsailingtracker://";

    private final MessageFormat htmlMailFormat;
    private final Locale locale;
    private final List<SerializableMimeBodyPartSupplier> mimeBodyPartSuppliers = new ArrayList<>();
    private final StringBuilder html = new StringBuilder();

    private String subject;

    RaceLogTrackingInvitationMailBuilder(final Locale locale) {
        this.locale = locale;
        this.htmlMailFormat = new MessageFormat("<!doctype html>\n<html><head></head><body>{0}</body></html>", locale);
    }

    RaceLogTrackingInvitationMailBuilder withSubject(final String invitee) {
        this.subject = RaceLogTrackingI18n.trackingInvitationFor(locale, invitee);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addHeadline(final Event event, final Leaderboard leaderboard) {
        this.html.append("<h1>");
        final String lbdn = leaderboard.getDisplayName() == null ? leaderboard.getName() : leaderboard.getDisplayName();
        this.html.append(RaceLogTrackingI18n.welcomeTo(locale, event.getName(), lbdn));
        this.html.append("</h1>");
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addSailInSightIntroductoryText(final String invitee) {
        return this.addIntroductoryText(RaceLogTrackingI18n.sailInSightAppName(locale), invitee);
    }

    RaceLogTrackingInvitationMailBuilder addBuoyPingerIntroductoryText(final String invitee) {
        return this.addIntroductoryText(RaceLogTrackingI18n.buoyPingerAppName(locale), invitee);
    }

    RaceLogTrackingInvitationMailBuilder addEventLogo(final Event event) {
        final List<ImageDescriptor> imagesWithTag = event.findImagesWithTag(MediaTagConstants.LOGO);
        if (imagesWithTag != null && !imagesWithTag.isEmpty()) {
            final String logoUrl = imagesWithTag.get(0).getURL().toString();
            this.html.append("<p><img src=\"").append(logoUrl).append("\"/></p>");
        }
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addQrCodeImage(final String url) {
        this.html.append("<p style=\"margin: 10px;height:250px; width: auto;\">");
        this.html.append("<img src=\"cid:image\" title=\"").append(url).append("\"/>");
        this.html.append("</p>");
        this.mimeBodyPartSuppliers.add(new QRCodeMimeBodyPartSupplier(url));
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addOpenInAppTextAndLinks(final String targetUrl, final String iOSAppUrl,
            final String androidAppUrl) {
        final boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        final boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            this.addSpacingTextBlock(RaceLogTrackingI18n::alternativelyVisitThisLink);
        }
        if (hasIOSAppUrl) {
            this.addLink(IOS_DEEP_LINK_PREFIX + targetUrl, RaceLogTrackingI18n::iOSUsers);
        }
        if (hasAndroidAppUrl) {
            this.addLink(targetUrl, RaceLogTrackingI18n::androidUsers);
        }
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addInstallAppTextAndLinks(final String iOSAppUrl, final String androidAppUrl) {
        final boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        final boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            this.addSpacingTextBlock(RaceLogTrackingI18n::appStoreInstallText);
        }
        if (hasIOSAppUrl) {
            this.addLink(iOSAppUrl, RaceLogTrackingI18n::appIos);
        }
        if (hasAndroidAppUrl) {
            this.addLink(androidAppUrl, RaceLogTrackingI18n::appAndroid);
        }
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addSpacer() {
        this.html.append("<p style=\"margin-top:50px;\"></p>");
        return this;
    }

    String getSubject() {
        return subject;
    }

    SerializableMultipartSupplier getMultipartSupplier() {
        final String content = htmlMailFormat.format(new Object[]{html.toString()}, new StringBuffer(), null).toString();
        mimeBodyPartSuppliers.add(0, new SerializableDefaultMimeBodyPartSupplier(content, "text/html"));
        return new SerializableMultipartSupplier("Invite",
                mimeBodyPartSuppliers.toArray(new SerializableMimeBodyPartSupplier[mimeBodyPartSuppliers.size()]));
    }

    private RaceLogTrackingInvitationMailBuilder addIntroductoryText(final String appName, final String invitee) {
        this.html.append("<p>");
        this.html.append(RaceLogTrackingI18n.scanQRCodeOrVisitUrlToRegisterAs(locale, appName));
        this.html.append(" <b>").append(invitee).append("</b>");
        this.html.append("</p>");
        return this;
    }

    private RaceLogTrackingInvitationMailBuilder addSpacingTextBlock(final Function<Locale, String> textFactory) {
        this.html.append("<p style=\"margin-top:50px;\">").append(textFactory.apply(locale)).append("</p>");
        return this;
    }

    private RaceLogTrackingInvitationMailBuilder addLink(final String url, final Function<Locale, String> textFactory) {
        this.html.append("<a href=\"").append(url).append("\" style=\"padding:15px; margin:10px; width:200px; ");
        this.html.append("display:inline-block; background-color:#337ab7; border-radius:4px; color:#ffffff; ");
        this.html.append("border:1px solid #2e6da4; text-decoration:none;\">");
        this.html.append(textFactory.apply(locale));
        this.html.append("</a>");
        return this;
    }
}
