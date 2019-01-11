package com.sap.sailing.domain.racelogtracking.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import com.sap.sse.shared.media.ImageDescriptor;

public abstract class RaceLogTrackingInvitationMailBuilder {
    private static final Logger LOG = Logger.getLogger(RaceLogTrackingInvitationMailBuilder.class.getName());
    /**
     * System separator can't be used, as the reader might use any mail client and OS. This line break seems to work
     * reliable over all systems.
     */
    protected static final String TEXT_LINE_BREAK = "\r\n";

    private String subject;
    protected final Locale locale;

    protected final Map<String, byte[]> pngAttachAndInline = new HashMap<>();
    protected final StringBuilder html = new StringBuilder();
    protected final StringBuilder text = new StringBuilder();

    public RaceLogTrackingInvitationMailBuilder(Locale locale) {
        this.locale = locale;
    }

    abstract protected void addIntroductoryText(String appName, String invitee);

    abstract public RaceLogTrackingInvitationMailBuilder addSailInsightDeeplink(String url, String legacyIOSAppUrl,
            String legacyAndroidAppUrl);

    abstract public RaceLogTrackingInvitationMailBuilder addBuoyPingerDeeplink(String url, String legacyIOSAppUrl,
            String legacyAndroidAppUrl);

    RaceLogTrackingInvitationMailBuilder withSubject(final String invitee) {
        this.subject = RaceLogTrackingI18n.trackingInvitationFor(locale, invitee);
        return this;
    }

    protected void insertImage(byte[] cidImage, String cidSource, String alt) {
        this.pngAttachAndInline.put(cidSource, cidImage);
        this.html.append("<img alt=\"").append(alt).append("\" src=\"cid:").append(cidSource).append("\"/>");
        this.html.append("<br>");
    }

    public String getSubject() {
        return subject;
    }

    RaceLogTrackingInvitationMailBuilder addEventLogo(final Event event) {
        final List<ImageDescriptor> imagesWithTag = event.findImagesWithTag(MediaTagConstants.LOGO.getName());
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

    RaceLogTrackingInvitationMailBuilder addHeadline(final Event event, final Leaderboard leaderboard) {
        final String lbdn = leaderboard.getDisplayName() == null ? leaderboard.getName() : leaderboard.getDisplayName();
        final String welcomeText = RaceLogTrackingI18n.welcomeTo(locale, event.getName(), lbdn);
        this.html.append("<h1>").append(welcomeText).append("</h1>");
        this.text.append(welcomeText).append(TEXT_LINE_BREAK);
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

    protected void addTextLink(String url, final Function<Locale, String> textFactory) {
        text.append(textFactory.apply(locale)).append(":");
        text.append(TEXT_LINE_BREAK);
        text.append(url);
        text.append(TEXT_LINE_BREAK);
        text.append(TEXT_LINE_BREAK);
    }
}
