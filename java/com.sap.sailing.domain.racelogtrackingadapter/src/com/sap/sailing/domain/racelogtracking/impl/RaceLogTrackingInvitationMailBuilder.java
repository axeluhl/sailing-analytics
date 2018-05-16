package com.sap.sailing.domain.racelogtracking.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.mail.QRCodeMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableDefaultMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableImageMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMultipartSupplier;
import com.sap.sse.qrcode.QRCodeGenerationUtil;
import com.sap.sse.shared.media.ImageDescriptor;

class RaceLogTrackingInvitationMailBuilder {

    /**
     * URL prefix, so the iOS app will recognize as deep link and pass anything after it to the app for analysis.
     */
    private static final String IOS_DEEP_LINK_PREFIX = "comsapsailingtracker://";

    private final Locale locale;
    private final List<SerializableMimeBodyPartSupplier> mimeBodyPartSuppliers = new ArrayList<>();
    private final StringBuilder html = new StringBuilder();

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
            final ImageDescriptor imageDescriptor = imagesWithTag.get(0);
            final String logoUrl = imageDescriptor.getURL().toString();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = new URL(logoUrl).openStream();) {
                BufferedImage image = ImageIO.read(is);
                ImageIO.write(image, "png", baos);
                String inlineImage = new String(Base64.getEncoder().encodeToString(baos.toByteArray()));
                String cidSource = "cid:logo";
                String base64Source ="data:image/png;base64," + inlineImage;
                this.html.append("<img src='"+cidSource+"'/>");
                this.html.append("<!--[if !mso]><!-- -->");
                this.html.append("<img src='"+base64Source+"'/>");
                this.html.append("<![endif]-->");
                this.mimeBodyPartSuppliers.add(new SerializableImageMimeBodyPartSupplier(baos.toByteArray(),"image/png","logo","logo.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addQrCodeImage(final String url) {
        try (DataInputStream imageIs = new DataInputStream(QRCodeGenerationUtil.create(url, 250))){
            byte[] targetArray = new byte[imageIs.available()];
            imageIs.readFully(targetArray);
            String inlineImage = new String(Base64.getEncoder().encodeToString(targetArray));
            String cidSource = "cid:image";
            String base64Source ="data:image/png;base64," + inlineImage;
            this.html.append("<img src='"+cidSource+"'/>");
            this.html.append("<!--[if !mso]><!-- -->");
            this.html.append("<img src='"+base64Source+"'/>");
            this.html.append("<![endif]-->");
            System.err.println();
            //            this.html.append("<img alt=\"no cid support\" src=\"cid:image\" title=\"").append(url).append("\"/>");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        this.html.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        this.html.append("<tr>");
        if (hasIOSAppUrl) {
            this.html.append("<td>");
            this.addLink(IOS_DEEP_LINK_PREFIX + targetUrl, RaceLogTrackingI18n::iOSUsers);
            this.html.append("</td>");
        }
        if (hasAndroidAppUrl) {
            this.html.append("<td>");
            this.addLink(targetUrl, RaceLogTrackingI18n::androidUsers);
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
        }
        this.html.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        this.html.append("<tr>");
        if (hasIOSAppUrl) {
            this.html.append("<td>");
            this.addLink(iOSAppUrl, RaceLogTrackingI18n::appIos);
            this.html.append("</td>");
        }
        if (hasAndroidAppUrl) {
            this.html.append("<td>");
            this.addLink(androidAppUrl, RaceLogTrackingI18n::appAndroid);
            this.html.append("</td>");
        }
        this.html.append("</tr>");
        this.html.append("</table>");
        return this;
    }

    RaceLogTrackingInvitationMailBuilder addSpacer() {
        this.html.append("<br><br>");
        return this;
    }

    String getSubject() {
        return subject;
    }

    SerializableMultipartSupplier getMultipartSupplier() {
        final String content = html.toString();
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
        this.html.append("<br><br><p>").append(textFactory.apply(locale)).append("</p>");
        return this;
    }

    private RaceLogTrackingInvitationMailBuilder addLink(final String url, final Function<Locale, String> textFactory) {
        this.html.append("<div style=\"background-color:#337ab7; border-radius:4px; border:1px solid #2e6da4; text-decoration:none;\">");
        this.html.append("<a href=\"").append(url).append("\" style=\"padding:15px; color:#ffffff; width:200px;\">");
        this.html.append(textFactory.apply(locale));
        this.html.append("</a>");
        this.html.append("</div>");
        return this;
    }
}
