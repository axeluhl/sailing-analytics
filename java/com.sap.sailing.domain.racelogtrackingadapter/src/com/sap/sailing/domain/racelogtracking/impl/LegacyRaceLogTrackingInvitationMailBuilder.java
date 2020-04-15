package com.sap.sailing.domain.racelogtracking.impl;

import java.io.DataInputStream;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.qrcode.QRCodeGenerationUtil;

/**
 * Builder to create invitation mails for competitor and buoy tracking. This class ensures that the mail is being sent
 * as text and html body to support a great variety of mail clients.
 */
class LegacyRaceLogTrackingInvitationMailBuilder extends RaceLogTrackingInvitationMailBuilder {
    private static final Logger LOG = Logger.getLogger(LegacyRaceLogTrackingInvitationMailBuilder.class.getName());

    /**
     * URL prefix, so the iOS app will recognize as deep link and pass anything after it to the app for analysis.
     */
    private static final String IOS_DEEP_LINK_PREFIX = "comsapsailingtracker://";

    /**
     * @param locale
     *            the locale in which the resulting mail will be generated. If there are no specific messages available
     *            for the given locale, English locale is used as fallback.
     */
    LegacyRaceLogTrackingInvitationMailBuilder(final Locale locale) {
        super(locale);
    }

    LegacyRaceLogTrackingInvitationMailBuilder addQrCodeImage(final String url) {
        try (DataInputStream imageIs = new DataInputStream(QRCodeGenerationUtil.create(url, 250))) {
            byte[] targetArray = new byte[imageIs.available()];
            imageIs.readFully(targetArray);
            insertImage(targetArray, "qr", url);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error while generating QR code for invitation mail", e);
        }
        return this;
    }

    LegacyRaceLogTrackingInvitationMailBuilder addOpenInAppTextAndLinks(final String targetUrl, final String iOSAppUrl,
            final String androidAppUrl) {
        final boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        final boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            this.html.append("<p>").append(RaceLogTrackingI18n.alternativelyVisitThisLink(this.locale)).append("</p>");
        }
        this.html.append("<table border=\"0\" cellspacing=\"20px\" cellpadding=\"0px\">");
        this.html.append("<tr>");
        if (hasIOSAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(IOS_DEEP_LINK_PREFIX + targetUrl, RaceLogTrackingI18n::iOSUsers);
            this.html.append("</td>");
            this.addTextLink(IOS_DEEP_LINK_PREFIX + targetUrl, RaceLogTrackingI18n::iOSUsers);
        }
        if (hasAndroidAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(targetUrl, RaceLogTrackingI18n::androidUsers);
            this.html.append("</td>");
            this.addTextLink(targetUrl, RaceLogTrackingI18n::androidUsers);
        }
        this.html.append("</tr>");
        this.html.append("</table>");
        return this;
    }

    LegacyRaceLogTrackingInvitationMailBuilder addInstallAppTextAndLinks(final String iOSAppUrl,
            final String androidAppUrl) {
        final boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        final boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            this.html.append("<p>").append(RaceLogTrackingI18n.appStoreInstallText(this.locale)).append("</p>");
            text.append(RaceLogTrackingI18n.appStoreInstallText(locale));
            text.append(TEXT_LINE_BREAK);
        }
        this.html.append("<table border=\"0\" cellspacing=\"20px\" cellpadding=\"0px\">");
        this.html.append("<tr>");
        if (hasIOSAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(iOSAppUrl, RaceLogTrackingI18n::appIos);
            this.html.append("</td>");
            this.addTextLink(iOSAppUrl, RaceLogTrackingI18n::appIos);
        }
        if (hasAndroidAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(androidAppUrl, RaceLogTrackingI18n::appAndroid);
            this.html.append("</td>");
            this.addTextLink(androidAppUrl, RaceLogTrackingI18n::appAndroid);
        }
        this.html.append("</tr>");
        this.html.append("</table>");
        return this;
    }

    @Override
    protected void addIntroductoryText(final String appName, final String invitee) {
        final String introText = RaceLogTrackingI18n.scanQRCodeOrVisitUrlToRegisterAs(locale, appName);
        this.html.append("<p>").append(introText).append(" <b>").append(invitee).append("</b></p>");
        this.text.append(introText).append(" ").append(invitee).append(TEXT_LINE_BREAK).append(TEXT_LINE_BREAK);
    }

    private void addHtmlLink(final String url, final Function<Locale, String> textFactory) {
        final String anchor = "<a href=\"" + url + "\" style=\"color:#fff; text-decoration:none;\">";
        // outer href for clickablity of the whole button
        this.html.append(anchor);
        // table being used for the styling, as some mail clients have isssues with divs
        this.html.append("<table width=\"100%\" style=\"background-color: #337ab7; color:#fff;\"><tr>");
        this.html.append("<td style=\"padding: 15px; text-align: center; color:#fff;\">");
        // inner href, so that outlook does work (it does not recognize the outer)
        this.html.append(anchor);
        this.html.append(textFactory.apply(locale));
        this.html.append("</a>");
        this.html.append("</td>");
        this.html.append("</tr></table>");
        this.html.append("</a>");
    }

    @Override
    public RaceLogTrackingInvitationMailBuilder addSailInsightDeeplink(String url, String iosAppUrl,
            String androidAppUrl) {
        this.addQrCodeImage(url) //
                .addOpenInAppTextAndLinks(url, iosAppUrl, androidAppUrl) //
                .addInstallAppTextAndLinks(iosAppUrl, androidAppUrl);
        return this;
    }

    @Override
    public RaceLogTrackingInvitationMailBuilder addBuoyPingerDeeplink(String url, String legacyIOSAppUrl,
            String legacyAndroidAppUrl) {
        this.addQrCodeImage(url) //
                .addOpenInAppTextAndLinks(url, legacyIOSAppUrl, legacyAndroidAppUrl) //
                .addInstallAppTextAndLinks(legacyIOSAppUrl, legacyAndroidAppUrl);
        return this;
    }
}
