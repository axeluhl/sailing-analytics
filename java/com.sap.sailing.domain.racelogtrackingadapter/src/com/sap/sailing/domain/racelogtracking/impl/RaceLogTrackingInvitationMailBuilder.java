package com.sap.sailing.domain.racelogtracking.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.mail.SerializableDefaultMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableFileMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMultipartMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMultipartSupplier;
import com.sap.sse.qrcode.QRCodeGenerationUtil;
import com.sap.sse.shared.media.ImageDescriptor;

/**
 * Builder to create invitation mails for competitor and buoy tracking. This class ensures that the mail is being sent
 * as text and html body to support a great variety of mail clients.
 */
class RaceLogTrackingInvitationMailBuilder {
    private static final Logger LOG = Logger.getLogger(RaceLogTrackingInvitationMailBuilder.class.getName());
    
    /**
     * System separator can't be used, as the reader might use any mail client and OS. This line break seems to work reliable over all
     * systems.
     */
    private static final String TEXT_LINE_BREAK = "\r\n";

    /**
     * URL prefix, so the iOS app will recognize as deep link and pass anything after it to the app for analysis.
     */
    private static final String IOS_DEEP_LINK_PREFIX = "comsapsailingtracker://";
    
    private static final String SAIL_INSIGHT_BRANCH_DEEPLINK = "https://d-labs.app.link/YUAdvnZjEO";

    private final Locale locale;
    private final Map<String,byte[]> pngAttachAndInline = new HashMap<>();
    private final StringBuilder html = new StringBuilder();
    private final StringBuilder text = new StringBuilder();

    private String subject;

    /**
     * @param locale
     *            the locale in which the resulting mail will be generated. If there are no specific messages available
     *            for the given locale, English locale is used as fallback.
     */
    RaceLogTrackingInvitationMailBuilder(final Locale locale) {
        this.locale = locale;
    }

    RaceLogTrackingInvitationMailBuilder withSubject(final String invitee) {
        this.subject = RaceLogTrackingI18n.trackingInvitationFor(locale, invitee);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addHeadline(final Event event, final Leaderboard leaderboard) {
        final String lbdn = leaderboard.getDisplayName() == null ? leaderboard.getName() : leaderboard.getDisplayName();
        final String welcomeText = RaceLogTrackingI18n.welcomeTo(locale, event.getName(), lbdn);
        this.html.append("<h1>").append(welcomeText).append("</h1>");
        this.text.append(welcomeText).append(TEXT_LINE_BREAK);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addSailInSightIntroductoryText(final String invitee) {
        this.addIntroductoryTextForBranchDeeplink(RaceLogTrackingI18n.sailInSightAppName(locale), invitee);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addBuoyPingerIntroductoryText(final String invitee) {
        this.addIntroductoryText(RaceLogTrackingI18n.buoyPingerAppName(locale), invitee);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addEventLogo(final Event event) {
        final List<ImageDescriptor> imagesWithTag = event.findImagesWithTag(MediaTagConstants.LOGO);
        if (imagesWithTag != null && !imagesWithTag.isEmpty()) {
            final ImageDescriptor imageDescriptor = imagesWithTag.get(0);
            final String logoUrl = imageDescriptor.getURL().toString();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = new URL(logoUrl).openStream();) {
                BufferedImage image = ImageIO.read(is);
                ImageIO.write(image, "png", baos);
                insertImage(baos.toByteArray(), "logo", logoUrl);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error while getting event image for invitation mail", e);
            }
        }
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addQrCodeImage(final String url) {
        try (DataInputStream imageIs = new DataInputStream(QRCodeGenerationUtil.create(url, 250))) {
            byte[] targetArray = new byte[imageIs.available()];
            imageIs.readFully(targetArray);
            insertImage(targetArray, "qr", url);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error while generating QR code for invitation mail", e);
        }
        return this;
    }
    
    RaceLogTrackingInvitationMailBuilder addQrCodeDeeplinkImage(final String url) {
    	String deeplink = String.format("%s?checkinUrl=%s", SAIL_INSIGHT_BRANCH_DEEPLINK, url);
    	return this.addQrCodeImage(deeplink);
    }

    RaceLogTrackingInvitationMailBuilder addOpenInAppTextAndLinks(final String targetUrl, final String iOSAppUrl,
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

    RaceLogTrackingInvitationMailBuilder addInstallAppTextAndLinks(final String iOSAppUrl, final String androidAppUrl) {
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
    
    RaceLogTrackingInvitationMailBuilder addSailInsightBranchDeeplink(final String checkinUrl) {
    	String deeplink = String.format("%s?checkinUrl=%s", SAIL_INSIGHT_BRANCH_DEEPLINK, checkinUrl);
        this.html.append("<table border=\"0\" cellspacing=\"20px\" cellpadding=\"0px\">");
        this.html.append("<tr>");
        this.html.append("<td>");
        this.addHtmlLink(deeplink, RaceLogTrackingI18n::register);
        this.html.append("</td>");
        this.addTextLink(deeplink, RaceLogTrackingI18n::register);
        this.html.append("</tr>");
        this.html.append("</table>");
    	return this;
    }

    String getSubject() {
        return subject;
    }

    SerializableMultipartSupplier getMultipartSupplier() throws MessagingException {
        final SerializableMultipartSupplier mixedSupplier = new SerializableMultipartSupplier("mixed");

        final SerializableMultipartSupplier alternativeSupplier = new SerializableMultipartSupplier("alternative");
        mixedSupplier.addBodyPart(new SerializableMultipartMimeBodyPartSupplier(alternativeSupplier));

        alternativeSupplier.addBodyPart(new SerializableDefaultMimeBodyPartSupplier(text.toString(), "text/plain"));
        
        final SerializableMultipartSupplier relatedSupplier = new SerializableMultipartSupplier("related");
        alternativeSupplier.addBodyPart(new SerializableMultipartMimeBodyPartSupplier(relatedSupplier));
        
        relatedSupplier.addBodyPart(new SerializableDefaultMimeBodyPartSupplier(html.toString(), "text/html"));
        
        for (Entry<String, byte[]> imageEntry : pngAttachAndInline.entrySet()) {
            final String contentType = "image/png";
            final String cid = "<" + imageEntry.getKey() + ">";
            final String filename = imageEntry.getKey() + ".png";
            final byte[] img = imageEntry.getValue();
            relatedSupplier.addBodyPart(new SerializableFileMimeBodyPartSupplier(img, contentType, cid, filename));
            mixedSupplier.addBodyPart(new SerializableFileMimeBodyPartSupplier(img, contentType, filename));
        }
        
        return mixedSupplier;
    }

    private void addIntroductoryText(final String appName, final String invitee) {
        final String introText = RaceLogTrackingI18n.scanQRCodeOrVisitUrlToRegisterAs(locale, appName);
        this.html.append("<p>").append(introText).append(" <b>").append(invitee).append("</b></p>");
        this.text.append(introText).append(" ").append(invitee).append(TEXT_LINE_BREAK).append(TEXT_LINE_BREAK);
    }
    
    private void addIntroductoryTextForBranchDeeplink(final String appName, final String invitee) {
        final String introText = RaceLogTrackingI18n.followBranchDeeplink(locale, appName, invitee);
        this.html.append("<p>").append(introText).append("</p>");
        this.text.append(introText).append(" ").append(invitee).append(TEXT_LINE_BREAK).append(TEXT_LINE_BREAK);
    }

    private void insertImage(byte[] cidImage, String cidSource, String alt) {
        this.pngAttachAndInline.put(cidSource, cidImage);
        this.html.append("<img alt=\"").append(alt).append("\" src=\"cid:").append(cidSource).append("\"/>");
        this.html.append("<br>");
    }

    private void addTextLink(String url, final Function<Locale, String> textFactory) {
        text.append(textFactory.apply(locale)).append(":");
        text.append(TEXT_LINE_BREAK);
        text.append(url);
        text.append(TEXT_LINE_BREAK);
        text.append(TEXT_LINE_BREAK);
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
}
