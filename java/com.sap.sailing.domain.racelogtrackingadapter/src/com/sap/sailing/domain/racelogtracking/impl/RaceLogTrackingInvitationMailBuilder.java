package com.sap.sailing.domain.racelogtracking.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

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

class RaceLogTrackingInvitationMailBuilder {
    /**
     * We cannot use the system seperator, as the reader might be another OS. This linebreak seems to work reliable over
     * all systems
     */
    private static final String TEXT_LINE_BREAK = "\r\n";

    /**
     * URL prefix, so the iOS app will recognize as deep link and pass anything after it to the app for analysis.
     */
    private static final String IOS_DEEP_LINK_PREFIX = "comsapsailingtracker://";

    private final Locale locale;
    private final Map<String,byte[]> pngAttachAndInline = new HashMap<>();
    private final StringBuilder html = new StringBuilder();
    private final StringBuilder text = new StringBuilder();

    private String subject;

    RaceLogTrackingInvitationMailBuilder(final Locale locale) {
        this.locale = locale;
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
        this.text.append(RaceLogTrackingI18n.welcomeTo(locale, event.getName(), lbdn));
        this.text.append(TEXT_LINE_BREAK);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addSailInSightIntroductoryText(final String invitee) {
        this.addIntroductoryText(RaceLogTrackingI18n.sailInSightAppName(locale), invitee);
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = new URL(logoUrl).openStream();) {
                BufferedImage image = ImageIO.read(is);
                ImageIO.write(image, "png", baos);
                String inlineImage = new String(Base64.getEncoder().encodeToString(baos.toByteArray()));
                insertImage(baos.toByteArray(), "logo", inlineImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * Inserts a image as both base64 and cid attachment. The caller must ensure the cid attachment is actually added to
     * the bodypartsuppliers.
     * 
     * @param cidSource
     *            cid image link
     * @param base64Source
     *            inline base64 data url Both are added, because most mail clients are only able to render one of them.
     * @throws MessagingException 
     */
    private void insertImage(byte[] cidImage, String cidSource, String inlineImage) throws MessagingException {
        pngAttachAndInline.put(cidSource, cidImage);
        this.html.append("<img alt='' src='cid:" + cidSource + "'/>");
        this.html.append("<br>");
    }

    RaceLogTrackingInvitationMailBuilder addQrCodeImage(final String url) {
        try (DataInputStream imageIs = new DataInputStream(QRCodeGenerationUtil.create(url, 250))) {
            byte[] targetArray = new byte[imageIs.available()];
            imageIs.readFully(targetArray);
            String inlineImage = new String(Base64.getEncoder().encodeToString(targetArray));
            insertImage(targetArray, "image", inlineImage);
            this.html.append("<a href=\"" + url + "\">");
            this.html.append(url);
            this.html.append("</a>");
            this.html.append("<br>");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.text.append(url);
        this.text.append(TEXT_LINE_BREAK);
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addOpenInAppTextAndLinks(final String targetUrl, final String iOSAppUrl,
            final String androidAppUrl) {
        final boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        final boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            this.addSpacingTextBlock(RaceLogTrackingI18n::alternativelyVisitThisLink);
        }
        this.html.append("<table border=\"0\" cellspacing=\"20px\" cellpadding=\"0px\">");
        this.html.append("<tr>");
        if (hasIOSAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(IOS_DEEP_LINK_PREFIX + targetUrl, RaceLogTrackingI18n::iOSUsers);
            this.html.append("</td>");
        }
        if (hasAndroidAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(targetUrl, RaceLogTrackingI18n::androidUsers);
            this.html.append("</td>");
        }
        this.html.append("</tr>");
        this.html.append("</table>");
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addInstallAppTextAndLinks(final String iOSAppUrl, final String androidAppUrl) {
        final boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        final boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            this.addSpacingTextBlock(RaceLogTrackingI18n::appStoreInstallText);
            text.append(RaceLogTrackingI18n.appStoreInstallText(locale));
            text.append(TEXT_LINE_BREAK);
        }
        this.html.append("<table border=\"0\" cellspacing=\"20px\" cellpadding=\"0px\">");
        this.html.append("<tr>");
        if (hasIOSAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(iOSAppUrl, RaceLogTrackingI18n::appIos);
            this.addTextLink(iOSAppUrl, RaceLogTrackingI18n::appIos);
            this.html.append("</td>");
        }
        if (hasAndroidAppUrl) {
            this.html.append("<td>");
            this.addHtmlLink(androidAppUrl, RaceLogTrackingI18n::appAndroid);
            this.addTextLink(androidAppUrl, RaceLogTrackingI18n::appAndroid);
            this.html.append("</td>");
        }
        this.html.append("</tr>");
        this.html.append("</table>");
        return this;
    }

    private void addTextLink(String url, final Function<Locale, String> textFactory) {
        text.append(textFactory.apply(locale));
        text.append(" ");
        text.append(url);
        text.append(TEXT_LINE_BREAK);
    }

    RaceLogTrackingInvitationMailBuilder addSpacer() {
        this.html.append("<br><br>");
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
        
        for (Entry<String, byte[]> img : pngAttachAndInline.entrySet()) {
            final String contentType = "image/png";
            final String contentId = "<" + img.getKey() + ">";
            final String filename = img.getKey() + ".png";
            relatedSupplier.addBodyPart(
                    new SerializableFileMimeBodyPartSupplier(img.getValue(), contentType, contentId, filename, false));
            mixedSupplier.addBodyPart(
                    new SerializableFileMimeBodyPartSupplier(img.getValue(), contentType, contentId, filename, true));
        }
        
        return mixedSupplier;
    }

    private void addIntroductoryText(final String appName, final String invitee) {
        this.html.append("<p>");
        this.html.append(RaceLogTrackingI18n.scanQRCodeOrVisitUrlToRegisterAs(locale, appName));
        this.html.append(" <b>").append(invitee).append("</b>");
        this.html.append("</p>");
        this.text.append(RaceLogTrackingI18n.scanQRCodeOrVisitUrlToRegisterAs(locale, appName));
        this.text.append(" ");
        this.text.append(invitee);
        this.text.append(TEXT_LINE_BREAK);
    }

    private void addSpacingTextBlock(final Function<Locale, String> textFactory) {
        this.html.append("<br><br><p>").append(textFactory.apply(locale)).append("</p><br>");
    }

    private void addHtmlLink(final String url,
            final Function<Locale, String> textFactory) {
        this.html.append(
                "<div style=\" background-color:#337ab7; border-radius:4px; border:10px solid #337ab7; text-decoration:none;\">");
        this.html.append("<a href=\"").append(url).append("\" style=\"padding:15px; color:#ffffff; width:200px;\">");
        this.html.append(textFactory.apply(locale));
        this.html.append("</a>");
        this.html.append("</div>");
    }
}
