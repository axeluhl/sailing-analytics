package com.sap.sailing.domain.racelogtracking.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.util.impl.NonGwtUrlHelper;

/**
 * Builder to create invitation mails for competitor and buoy tracking. This class ensures that the mail is being sent
 * as text and html body to support a great variety of mail clients.
 */
abstract class BranchIORaceLogTrackingInvitationMailBuilder extends RaceLogTrackingInvitationMailBuilder {
    private static final Logger LOG = Logger.getLogger(BranchIORaceLogTrackingInvitationMailBuilder.class.getName());
    
    /**
     * @param locale
     *            the locale in which the resulting mail will be generated. If there are no specific messages available
     *            for the given locale, English locale is used as fallback.
     */
    BranchIORaceLogTrackingInvitationMailBuilder(final Locale locale) {
        super(locale);
    }

    BranchIORaceLogTrackingInvitationMailBuilder addEventLogo(final Event event) {
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
    
    @Override
    public RaceLogTrackingInvitationMailBuilder addSailInsightDeeplink(final String checkinUrl, String legacy1,
            String legacy2) {
        String deeplink = String.format("%s?%s=%s", getSailInsightBranchIO(),
                DeviceMappingConstants.URL_CHECKIN_URL, NonGwtUrlHelper.INSTANCE.encodeQueryString(checkinUrl));
        this.addDeeplink(deeplink, RaceLogTrackingI18n::register);
        return this;
    }
    
    protected abstract String getSailInsightBranchIO();

    @Override
    public RaceLogTrackingInvitationMailBuilder addBuoyPingerDeeplink(final String checkinUrl, String legacy1,
            String legacy2) {
        String deeplink = String.format("%s?%s=%s", getBouyPingerBranchIO(),
                DeviceMappingConstants.URL_CHECKIN_URL, NonGwtUrlHelper.INSTANCE.encodeQueryString(checkinUrl));
        this.addDeeplink(deeplink, RaceLogTrackingI18n::register);
        return this;
    }
    
    abstract protected String getBouyPingerBranchIO();

    private void addDeeplink(final String url, final Function<Locale, String> textFactory) {
        this.html.append("<table border=\"0\" cellspacing=\"20px\" cellpadding=\"0px\">");
        this.html.append("<tr>");
        this.html.append("<td>");
        this.addHtmlLink(url, textFactory);
        this.html.append("</td>");
        this.addTextLink(url, textFactory);
        this.html.append("</tr>");
        this.html.append("</table>");
    }
    
    @Override
    protected void addIntroductoryText(final String appName, final String invitee) {
        final String introText = RaceLogTrackingI18n.followBranchDeeplink(locale, appName, invitee);
        this.html.append("<p>").append(introText).append("</p>");
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
    
}
