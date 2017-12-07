package com.sap.sailing.domain.racelogtracking.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import javax.mail.MessagingException;

import org.osgi.framework.ServiceReference;

import com.sap.sailing.domain.abstractlog.impl.LastEventOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDenoteForTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogStartTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogRaceTrackerExistsException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.QRCodeMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableDefaultMimeBodyPartSupplier;
import com.sap.sse.mail.SerializableMultipartSupplier;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.util.impl.NonGwtUrlHelper;

public class RaceLogTrackingAdapterImpl implements RaceLogTrackingAdapter {
    private static final Logger logger = Logger.getLogger(RaceLogTrackingAdapterImpl.class.getName());

    /**
     * The URL prefix that the iOS app will recognize as a deep link and pass anything after this prefix on
     * to the app for analysis
     */
    private static final String IOS_DEEP_LINK_PREFIX = "comsapsailingtracker://";

    private final DomainFactory domainFactory;
    private final long delayToLiveInMillis;

    public RaceLogTrackingAdapterImpl(DomainFactory domainFactory) {
        this.domainFactory = domainFactory;
        this.delayToLiveInMillis = TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS;
    }

    @Override
    public RaceHandle startTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, boolean trackWind, boolean correctWindDirectionByMagneticDeclination)
            throws NotDenotedForRaceLogTrackingException, Exception {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        RaceLogTrackingState raceLogTrackingState = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
        if (!raceLogTrackingState.isForTracking()) {
            throw new NotDenotedForRaceLogTrackingException();
        }
        RegattaIdentifier regatta = ((RegattaLeaderboard) leaderboard).getRegatta().getRegattaIdentifier();
        if (raceLogTrackingState != RaceLogTrackingState.TRACKING) {
            RaceLogEvent event = new RaceLogStartTrackingEventImpl(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId());
            raceLog.add(event);
        }
        final RaceHandle result;
        if (!isRaceLogRaceTrackerAttached(service, raceLog)) {
            result = addTracker(service, regatta, leaderboard, raceColumn, fleet, -1, trackWind, correctWindDirectionByMagneticDeclination);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Adds a {@link RaceLogRaceTracker}. If a {@link RaceLogStartTrackingEvent} is already present in the
     * {@code RaceLog} linked to the {@code raceColumn} and {@code fleet}, a {@code TrackedRace} is created immediately
     * and tracking begins. Otherwise, the {@code RaceLogRaceTracker} waits until a {@code StartTrackingEvent} is added
     * to perform these actions. The race first has to be denoted for racelog tracking.
     */
    private RaceHandle addTracker(RacingEventService service, RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
            RaceColumn raceColumn, Fleet fleet, long timeoutInMilliseconds, boolean trackWind,
            boolean correctWindDirectionByMagneticDeclination) throws RaceLogRaceTrackerExistsException, Exception {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        assert !isRaceLogRaceTrackerAttached(service, raceLog) : new RaceLogRaceTrackerExistsException(
                leaderboard.getName() + " - " + raceColumn.getName() + " - " + fleet.getName());
        Regatta regatta = regattaToAddTo == null ? null : service.getRegatta(regattaToAddTo);
        RaceLogConnectivityParams params = new RaceLogConnectivityParams(service, regatta, raceColumn, fleet,
                leaderboard, delayToLiveInMillis, domainFactory, trackWind, correctWindDirectionByMagneticDeclination);
        return service.addRace(regattaToAddTo, params, timeoutInMilliseconds);
    }

    @Override
    public boolean denoteRaceForRaceLogTracking(RacingEventService service, Leaderboard leaderboard,
            RaceColumn raceColumn, Fleet fleet, String raceName) throws NotDenotableForRaceLogTrackingException {
        final BoatClass boatClass;
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard rLeaderboard = (RegattaLeaderboard) leaderboard;
            boatClass = rLeaderboard.getRegatta().getBoatClass();
        } else {
            if (!Util.isEmpty(raceColumn.getAllCompetitorsAndTheirBoats(fleet).values())) {
                boatClass = findDominatingBoatClass(raceColumn.getAllCompetitorsAndTheirBoats(fleet).values());
            } else if (!Util.isEmpty(raceColumn.getAllCompetitorsAndTheirBoats().values())) {
                boatClass = findDominatingBoatClass(raceColumn.getAllCompetitorsAndTheirBoats().values());
            } else if (!Util.isEmpty(leaderboard.getAllCompetitors())) {
                boatClass = leaderboard.getBoatClass();
            } else { 
                throw new NotDenotableForRaceLogTrackingException("Couldn't infer boat class, no competitors on race and leaderboard");
            }
        }
        final boolean result;
        if (raceName == null) {
            raceName = leaderboard.getName() + " " + raceColumn.getName() + " " + fleet.getName();
        }
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog == null) {
            throw new NotDenotableForRaceLogTrackingException("No RaceLog found in place");
        }
        if (new RaceLogTrackingStateAnalyzer(raceLog).analyze().isForTracking()) {
            result = false;
        } else {
            RaceLogEvent event = new RaceLogDenoteForTrackingEventImpl(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId(), raceName, boatClass, UUID.randomUUID());
            raceLog.add(event);
            result = true;
        }
        return result;
    }
    
    private BoatClass findDominatingBoatClass(Iterable<Boat> allBoats) {
        return Util.getDominantObject(()->StreamSupport.stream(allBoats.spliterator(), /* parallel */ false).map(b->b.getBoatClass()).iterator());
    }

    @Override
    public void denoteAllRacesForRaceLogTracking(final RacingEventService service, final Leaderboard leaderboard)
            throws NotDenotableForRaceLogTrackingException {
        for (RaceColumn column : leaderboard.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                denoteRaceForRaceLogTracking(service, leaderboard, column, fleet, null);
            }
        }
    }

    @Override
    public boolean isRaceLogRaceTrackerAttached(RacingEventService service, RaceLog raceLog) {
        return service.getRaceTrackerById(raceLog.getId()) != null;
    }

    @Override
    public RaceLogTrackingState getRaceLogTrackingState(RacingEventService service, RaceColumn raceColumn, Fleet fleet) {
        return new RaceLogTrackingStateAnalyzer(raceColumn.getRaceLog(fleet)).analyze();
    }

    @Override
    public void copyCourse(RaceLog fromRaceLog, Set<RaceLog> toRaceLogs, SharedDomainFactory baseDomainFactory,
            RacingEventService service) {
        CourseBase course = new LastPublishedCourseDesignFinder(fromRaceLog, /* onlyCoursesWithValidWaypointList */ true).analyze();
        final Set<Mark> marks = new HashSet<>();
        if (course != null) {
            course.getWaypoints().forEach(wp -> Util.addAll(wp.getMarks(), marks));
        }

        for (RaceLog toRaceLog : toRaceLogs) {
            if (new RaceLogTrackingStateAnalyzer(toRaceLog).analyze().isForTracking()) {
                if (course != null) {
                    CourseBase newCourse = new CourseDataImpl(course.getName());
                    TimePoint now = MillisecondsTimePoint.now();
                    int i = 0;
                    for (Waypoint oldWaypoint : course.getWaypoints()) {
                        newCourse.addWaypoint(i++, oldWaypoint);
                    }

                    int passId = toRaceLog.getCurrentPassId();
                    RaceLogEvent newCourseEvent = new RaceLogCourseDesignChangedEventImpl(now,
                            service.getServerAuthor(), passId, newCourse, CourseDesignerMode.ADMIN_CONSOLE);
                    toRaceLog.add(newCourseEvent);
                }

            }
        }
    }

    @Override
    public void copyCompetitors(final RaceColumn fromRaceColumn, final Fleet fromFleet, final Iterable<Pair<RaceColumn, Fleet>> toRaces) {
        Map<Competitor, Boat> competitorsAndBoatsToCopy = fromRaceColumn.getAllCompetitorsAndTheirBoats(fromFleet);
        for (Pair<RaceColumn, Fleet> toRace : toRaces) {
            final RaceColumn toRaceColumn = toRace.getA();
            final Fleet toFleet = toRace.getB();
            try {
                if (toRaceColumn.isCompetitorRegistrationInRacelogEnabled(toFleet)) {
                    toRaceColumn.registerCompetitors(competitorsAndBoatsToCopy, toFleet);
                } else {
                    toRaceColumn.enableCompetitorRegistrationOnRaceLog(toFleet);
                    toRaceColumn.registerCompetitors(competitorsAndBoatsToCopy, toFleet);
                }
            } catch (CompetitorRegistrationOnRaceLogDisabledException e1) {
                // cannot happen as we explicitly checked successfully before, or enabled it when the check failed; still produce a log documenting this strangeness:
                logger.log(Level.WARNING, "Internal error: race column "+toRaceColumn.getName()+" does not accept competitor registration although it should", e1);
            }
        }
    }

    @Override
    public void pingMark(RegattaLog log, Mark mark, GPSFix gpsFix, RacingEventService service) {
        final PingDeviceIdentifierImpl device = new PingDeviceIdentifierImpl();
        final TimePoint timePoint = gpsFix.getTimePoint();
        final TimePoint now = MillisecondsTimePoint.now();
        final RegattaLogEvent mapping = new RegattaLogDeviceMarkMappingEventImpl(now,
                now, service.getServerAuthor(), UUID.randomUUID(), mark, device, timePoint, timePoint);
        log.add(mapping);
        try {
            service.getSensorFixStore().storeFix(device, gpsFix);
        } catch (NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not ping mark " + mark);
        }
    }

    @Override
    public void removeDenotationForRaceLogTracking(RacingEventService service, RaceLog raceLog) {
        RaceLogEvent denoteForTrackingEvent = new LastEventOfTypeFinder<>(raceLog, true,
                RaceLogDenoteForTrackingEvent.class).analyze();
        RaceLogEvent startTrackingEvent = new LastEventOfTypeFinder<>(raceLog, true, RaceLogStartTrackingEvent.class)
                .analyze();
        try {
            raceLog.revokeEvent(service.getServerAuthor(), denoteForTrackingEvent, "remove denotation");
            raceLog.revokeEvent(service.getServerAuthor(), startTrackingEvent,
                    "reset start time upon removing denotation");
        } catch (NotRevokableException e) {
            logger.log(Level.WARNING, "could not remove denotation by adding RevokeEvents", e);
        }
    }

    private MailService getMailService() {
        ServiceReference<MailService> ref = Activator.getContext().getServiceReference(MailService.class);
        if (ref == null) {
            logger.warning("No file storage management service registered");
            return null;
        }
        return Activator.getContext().getService(ref);
    }

    @Override
    public void inviteCompetitorsForTrackingViaEmail(Event event, Leaderboard leaderboard,
            String serverUrlWithoutTrailingSlash, Set<Competitor> competitors, String iOSAppUrl, String androidAppUrl,
            Locale locale) throws MailException {
        StringBuilder occuredExceptions = new StringBuilder();
        for (Competitor competitor : competitors) {
            final String toAddress = competitor.getEmail();
            if (toAddress != null) {
                String leaderboardDisplayName = leaderboard.getDisplayName() == null ? leaderboard.getName() : leaderboard.getDisplayName();
                String competitorName = competitor.getName();
                String url = DeviceMappingConstants.getDeviceMappingForRegattaLogUrl(serverUrlWithoutTrailingSlash,
                        event.getId().toString(), leaderboard.getName(), DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING,
                        competitor.getId().toString(), NonGwtUrlHelper.INSTANCE);
                String logoUrl = null;
                final List<ImageDescriptor> imagesWithTag = event.findImagesWithTag(MediaTagConstants.LOGO);
                if (imagesWithTag != null && !imagesWithTag.isEmpty()) {
                    logoUrl = imagesWithTag.get(0).getURL().toString();
                }
                try {
                    final ResourceBundleStringMessages B = RaceLogTrackingI18n.STRING_MESSAGES;
                    sendInvitationEmail(locale, toAddress, leaderboardDisplayName, event.getName(), competitorName, url,
                            B.get(locale, "sailInSightAppName"), iOSAppUrl, androidAppUrl, logoUrl);
                } catch (MailException e) {
                    occuredExceptions.append(e.getMessage() + "\r\n");
                }
            }
        }
        if (!(occuredExceptions.length() == 0)) {
            throw new MailException(occuredExceptions.toString());
        }
    }

    private void sendInvitationEmail(Locale locale, final String toAddress, String leaderboardName, String eventName,
            String invitee, String url, String appName, String iOSAppUrl, String androidAppUrl, String logoUrl) throws MailException {
        final ResourceBundleStringMessages B = RaceLogTrackingI18n.STRING_MESSAGES;
        String subject = String.format("%s %s", B.get(locale, "trackingInvitationFor"), invitee);

        boolean hasIOSAppUrl = iOSAppUrl != null && !iOSAppUrl.isEmpty();
        boolean hasAndroidAppUrl = androidAppUrl != null && !androidAppUrl.isEmpty();
        boolean hasLogoUrl = logoUrl != null && !logoUrl.isEmpty();
        StringBuilder htmlText = new StringBuilder();
        htmlText.append("<!doctype html>\n");
        htmlText.append("<html>");
        final String welcomeText = B.get(locale, "welcomeTo")+" "+eventName+", "+leaderboardName;
        htmlText.append("<head><title>").append(welcomeText).append("</title>");
        htmlText.append("<style type=\"text/css\">\n");
        htmlText.append(".b,.b:active,.b:visited {padding:15px;margin:10px;width:200px;display:inline-block;background-color:#337ab7;border-radius:4px;color:#ffffff;border:1px solid #2e6da4;text-decoration:none;}\n");
        htmlText.append(".b:hover {background-color:#2b618e;border:1px solid #204d74;}\n");
        htmlText.append(".qr {margin: 10px;height:250px; width: auto;}\n");
        htmlText.append(".spacer {margin-top: 50px;}\n");
        htmlText.append("</style>");
        htmlText.append("</head>");
        htmlText.append("<body>");
        if (hasLogoUrl) {
            htmlText.append("<p><img src=\"").append(logoUrl).append("\" /></p> ");
        }
        htmlText.append("<h1>").append(welcomeText).append("</h1> ");
        htmlText.append("<p>").append(B.get(locale, "scanQRCodeOrVisitUrlToRegisterAs", appName)).append(" <b>").append(invitee)
                .append("</b></p> ");
        htmlText.append("<p class=\"qr\"><img src=\"cid:image\"  title=\"").append(url).append("\" /></p> ");
        htmlText.append("<p class=\"spacer\">").append(B.get(locale, "alternativelyVisitThisLink")).append("</p> ");
        if (hasIOSAppUrl) {
            htmlText.append("<a class=\"b\" href=\"").append(IOS_DEEP_LINK_PREFIX + url).append("\">")
                    .append(B.get(locale, "iOSUsers")).append("</a> ");
        }
        if (hasAndroidAppUrl) {
            htmlText.append("<a class=\"b\" href=\"").append(url).append("\">").append(B.get(locale, "androidUsers"))
                    .append("</a> ");
        }
        if (hasIOSAppUrl || hasAndroidAppUrl) {
            htmlText.append("<p class=\"spacer\">").append(B.get(locale, "appStoreInstallText")).append("</p> ");
        }
        if (hasIOSAppUrl) {
            htmlText.append("<a class=\"b\" href=\"").append(iOSAppUrl).append("\">").append(B.get(locale, "appIos"))
                    .append("</a> ");
        }
        if (hasAndroidAppUrl) {
            htmlText.append("<a class=\"b\" href=\"").append(androidAppUrl).append("\">")
                    .append(B.get(locale, "appAndroid")).append("</a> ");
        }
        htmlText.append("<p class=\"spacer\"></p>");
        htmlText.append("</body>");
        htmlText.append("</html>");

        try {
            final SerializableMultipartSupplier multipartSupplier = new SerializableMultipartSupplier("Invite",
                    new SerializableDefaultMimeBodyPartSupplier(htmlText.toString(), "text/html"),
                    new QRCodeMimeBodyPartSupplier(url));
            getMailService().sendMail(toAddress, subject, multipartSupplier);
        } catch (MessagingException | MailException | IOException e) {
            logger.log(Level.SEVERE, "Error trying to send mail to " + invitee + " with e-mail address " + toAddress, e);
            throw new MailException(e.getMessage());
        }
    }
    
    @Override
    public void inviteBuoyTenderViaEmail(Event event, Leaderboard leaderboard, String serverUrlWithoutTrailingSlash,
            String emails, String iOSAppUrl, String androidAppUrl, Locale locale) throws MailException {
        StringBuilder occuredExceptions = new StringBuilder();
        String[] emailArray = emails.split(",");
        String leaderboardDisplayName = leaderboard.getDisplayName() == null ? leaderboard.getName() : leaderboard.getDisplayName();
        String eventId = event.getId().toString();
        String logoUrl = null;
        List<ImageDescriptor> imagesWithTag = event.findImagesWithTag(MediaTagConstants.LOGO);
        if (imagesWithTag != null && !imagesWithTag.isEmpty()) {
            logoUrl = imagesWithTag.get(0).getURL().toString();
        }
        // http://<host>/buoy-tender/checkin?event_id=<event-id>&leaderboard_name=<leaderboard-name>
        String url = DeviceMappingConstants.getBuoyTenderInvitationUrl(serverUrlWithoutTrailingSlash, leaderboard.getName(),
                eventId, NonGwtUrlHelper.INSTANCE);
        for (String toAddress : emailArray) {
            try {
                final ResourceBundleStringMessages B = RaceLogTrackingI18n.STRING_MESSAGES;
                sendInvitationEmail(locale, toAddress, leaderboardDisplayName,
                        event.getName(), RaceLogTrackingI18n.STRING_MESSAGES.get(locale, "buoyTender"), url, B.get(locale, "buoyPingerAppName"),
                        iOSAppUrl, androidAppUrl, logoUrl);
            } catch (MailException e) {
                occuredExceptions.append(e.getMessage() + "\r\n");
            }
        }
        if (!(occuredExceptions.length() == 0)) {
            throw new MailException(occuredExceptions.toString());
        }
    }
}
