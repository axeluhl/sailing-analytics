package com.sap.sailing.domain.racelogtracking.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.osgi.framework.ServiceReference;

import com.google.zxing.WriterException;
import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.impl.LastEventOfTypeFinder;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.shared.analyzing.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
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
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogRaceTrackerExistsException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.qrcode.QRCodeGenerationUtil;
import com.sap.sse.mail.MailService;
import com.sap.sse.util.impl.NonGwtUrlHelper;

public class RaceLogTrackingAdapterImpl implements RaceLogTrackingAdapter {
    private static final Logger logger = Logger.getLogger(RaceLogTrackingAdapterImpl.class.getName());

    private final DomainFactory domainFactory;
    private final long delayToLiveInMillis;

    public RaceLogTrackingAdapterImpl(DomainFactory domainFactory) {
        this.domainFactory = domainFactory;
        this.delayToLiveInMillis = TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS;
    }

    @Override
    public void startTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet)
            throws NotDenotedForRaceLogTrackingException, Exception {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        RaceLogTrackingState raceLogTrackingState = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
        if (! raceLogTrackingState.isForTracking()) {
            throw new NotDenotedForRaceLogTrackingException();
        }
        RegattaIdentifier regatta = ((RegattaLeaderboard) leaderboard).getRegatta().getRegattaIdentifier();

        if (raceLogTrackingState != RaceLogTrackingState.TRACKING) {
            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartTrackingEvent(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId());
            raceLog.add(event);
        }

        if (! isRaceLogRaceTrackerAttached(service, raceLog)) {
            addTracker(service, regatta, leaderboard, raceColumn, fleet, -1);
        }
    }  

    /**
     * Adds a {@link RaceLogRaceTracker}. If a {@link RaceLogStartTrackingEvent} is already present in the {@code RaceLog}
     * linked to the {@code raceColumn} and {@code fleet}, a {@code TrackedRace} is created immediately and tracking begins.
     * Otherwise, the {@code RaceLogRaceTracker} waits until a {@code StartTrackingEvent} is added to perform these actions.
     * The race first has to be denoted for racelog tracking.
     */
    private RaceHandle addTracker(RacingEventService service, RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
            RaceColumn raceColumn, Fleet fleet, long timeoutInMilliseconds) throws RaceLogRaceTrackerExistsException, Exception {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        assert ! isRaceLogRaceTrackerAttached(service, raceLog) : new RaceLogRaceTrackerExistsException(
                leaderboard.getName() + " - " + raceColumn.getName() + " - " + fleet.getName());

        Regatta regatta = regattaToAddTo == null ? null : service.getRegatta(regattaToAddTo);
        RaceLogConnectivityParams params = new RaceLogConnectivityParams(service, regatta, raceColumn, fleet,
                leaderboard, delayToLiveInMillis, domainFactory);
        return service.addRace(regattaToAddTo, params, timeoutInMilliseconds);
    }

    @Override
    public void denoteRaceForRaceLogTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, String raceName) throws NotDenotableForRaceLogTrackingException {

        BoatClass boatClass = null;
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard rLeaderboard = (RegattaLeaderboard) leaderboard;
            boatClass = rLeaderboard.getRegatta().getBoatClass();
        } else {
            throw new NotDenotableForRaceLogTrackingException("Can only denote races in RegattaLeaderboards for RaceLog-tracking");
        }

        if (raceName == null) {
            raceName = leaderboard.getName() + " " + raceColumn.getName() + " " + fleet.getName();
        }

        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        assert raceLog != null : new NotDenotableForRaceLogTrackingException("No RaceLog found in place");

        if (new RaceLogTrackingStateAnalyzer(raceLog).analyze().isForTracking()) {
            throw new NotDenotableForRaceLogTrackingException("Already denoted for tracking");
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDenoteForTrackingEvent(MillisecondsTimePoint.now(),
                service.getServerAuthor(), raceLog.getCurrentPassId(), raceName, boatClass, UUID.randomUUID());
        raceLog.add(event);
    }

    @Override
    public void denoteAllRacesForRaceLogTracking(final RacingEventService service, final Leaderboard leaderboard)
            throws NotDenotableForRaceLogTrackingException {
        if (leaderboard instanceof FlexibleLeaderboard) {
            throw new NotDenotableForRaceLogTrackingException("Can only use regatta leaderboards for RaceLog-tracking");
        }
        for (RaceColumn column : leaderboard.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                try {
                    denoteRaceForRaceLogTracking(service, leaderboard, column, fleet, null);
                } catch (NotDenotableForRaceLogTrackingException e) {}
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

    private Waypoint duplicateWaypoint(Waypoint waypoint, Map<ControlPoint, ControlPoint> controlPointDuplicationCache,
            Map<Mark, Mark> markDuplicationCache, SharedDomainFactory baseDomainFactory) {
        ControlPoint oldCP = waypoint.getControlPoint();
        ControlPoint newCP = null;
        PassingInstruction pi = waypoint.getPassingInstructions();
        if (controlPointDuplicationCache.get(oldCP) != null) {
            newCP = controlPointDuplicationCache.get(oldCP);
        } else {
            Mark[] newMarks = new Mark[Util.size(oldCP.getMarks())];
            int i = 0;
            for (Mark oldMark : oldCP.getMarks()) {
                newMarks[i] = null;
                if (markDuplicationCache.get(oldMark) != null) {
                    newMarks[i] = markDuplicationCache.get(oldMark);
                } else {
                    newMarks[i] = baseDomainFactory.getOrCreateMark(UUID.randomUUID(), oldMark.getName(), oldMark.getType(),
                            oldMark.getColor(), oldMark.getShape(), oldMark.getPattern());
                    markDuplicationCache.put(oldMark, newMarks[i]);
                }
                i++;
            }
            switch (newMarks.length) {
            case 1:
                newCP = newMarks[0];
                break;
            case 2:
                newCP = baseDomainFactory.createControlPointWithTwoMarks(newMarks[0], newMarks[1], oldCP.getName());
                break;
            default:
                logger.log(Level.WARNING, "Don't know how to duplicate CP with more than 2 marks");
                throw new RuntimeException("Don't know how to duplicate CP with more than 2 marks");
            }
            controlPointDuplicationCache.put(oldCP, newCP);
        }

        return baseDomainFactory.createWaypoint(newCP, pi);
    }
    
    private void revokeAlreadyDefinedMarks(RaceLog raceLog, AbstractLogEventAuthor author) {
        List<RaceLogEvent> markEvents = new AllEventsOfTypeFinder<>(raceLog, true, RaceLogDefineMarkEvent.class).analyze();
        for (RaceLogEvent event : markEvents) {
            try {
                raceLog.revokeEvent(author, event, "removing mark that was already defined");
            } catch (NotRevokableException e) {
                logger.log(Level.WARNING, "Could not remove mark that was already defined by adding RevokeEvent", e);
            }
        }
    }

    @Override
    public void copyCourseAndCompetitors(RaceLog fromRaceLog, Set<RaceLog> toRaceLogs, SharedDomainFactory baseDomainFactory,
            RacingEventService service) {
        CourseBase course = new LastPublishedCourseDesignFinder(fromRaceLog).analyze();
        Set<Competitor> competitors = new RegisteredCompetitorsAnalyzer<>(fromRaceLog).analyze();

        for (RaceLog toRaceLog : toRaceLogs) {
            if (course == null || ! new RaceLogTrackingStateAnalyzer(toRaceLog).analyze().isForTracking()) {
                continue;
            }
            CourseBase to = new CourseDataImpl("Copy of \"" + course.getName());
            TimePoint now = MillisecondsTimePoint.now();
            int i = 0;
            Map<ControlPoint, ControlPoint> controlPointDuplicationCache = new HashMap<ControlPoint, ControlPoint>();
            revokeAlreadyDefinedMarks(toRaceLog, service.getServerAuthor());
            Map<Mark, Mark> markDuplicationCache = new HashMap<Mark, Mark>();
            for (Waypoint oldWaypoint : course.getWaypoints()) {
                to.addWaypoint(i++, duplicateWaypoint(oldWaypoint, controlPointDuplicationCache, markDuplicationCache, baseDomainFactory));
            }

            for (Mark mark : markDuplicationCache.values()) {
                RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDefineMarkEvent(now, service.getServerAuthor(),
                        toRaceLog.getCurrentPassId(), mark);
                toRaceLog.add(event);
            }

            int passId = toRaceLog.getCurrentPassId();

            RaceLogEvent newCourseEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(
                    now, service.getServerAuthor(), passId, to);
            toRaceLog.add(newCourseEvent);

            registerCompetitors(service, toRaceLog, competitors);
        }
    }

    @Override
    public void pingMark(RaceLog raceLog, Mark mark, GPSFix gpsFix, RacingEventService service) {
        DeviceIdentifier device = new PingDeviceIdentifierImpl();
        TimePoint time = gpsFix.getTimePoint();

        RaceLogEvent mapping = RaceLogEventFactory.INSTANCE.createDeviceMarkMappingEvent(time,
                service.getServerAuthor(), device, mark, raceLog.getCurrentPassId(), time, time);
        raceLog.add(mapping);
        try {
            service.getGPSFixStore().storeFix(device, gpsFix);
        } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not pint mark " + mark);
        }
    }

    @Override
    public void removeDenotationForRaceLogTracking(RacingEventService service, RaceLog raceLog) {
        RaceLogEvent denoteForTrackingEvent = new LastEventOfTypeFinder<>(raceLog, true, RaceLogDenoteForTrackingEvent.class).analyze();
        RaceLogEvent startTrackingEvent = new LastEventOfTypeFinder<>(raceLog, true, RaceLogStartTrackingEvent.class).analyze();
        try {
            raceLog.revokeEvent(service.getServerAuthor(), denoteForTrackingEvent, "remove denotation");
            raceLog.revokeEvent(service.getServerAuthor(), startTrackingEvent, "reset start time upon removing denotation");
        } catch (NotRevokableException e) {
            logger.log(Level.WARNING, "could not remove denotation by adding RevokeEvents", e);
        }
    }
    
    private <LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        void registerCompetitors(AbstractLogEventAuthor author, LogT log, Set<Competitor> competitors,
                Function<Competitor, EventT> registerEventFactory) {
        Set<Competitor> alreadyRegistered = new HashSet<Competitor>(new RegisteredCompetitorsAnalyzer<>(log).analyze());
        Set<Competitor> toBeRegistered = new HashSet<Competitor>();
        
        for (Competitor c : competitors) {
            toBeRegistered.add(c);
        }
        
        Set<Competitor> toBeRemoved = new HashSet<Competitor>(alreadyRegistered);
        toBeRemoved.removeAll(toBeRegistered);
        toBeRegistered.removeAll(alreadyRegistered);
        
        //register
        for (Competitor c : toBeRegistered) {
            log.add(registerEventFactory.apply(c));
        }
        
        //unregister
        for (EventT event : log.getUnrevokedEventsDescending()) {
            if (event instanceof RegisterCompetitorEvent) {
                RegisterCompetitorEvent<?> registerEvent = (RegisterCompetitorEvent<?>) event;
                if (toBeRemoved.contains(registerEvent.getCompetitor())) {
                    try {
                        log.revokeEvent(author, event,
                        "unregistering competitor because no longer selected for registration");
                    } catch (NotRevokableException e) {
                        logger.log(Level.WARNING, "could not unregister competitor by adding RevokeEvent", e);
                    }
                }
            }
        }   
    }

    @Override
    public void registerCompetitors(RacingEventService service, RaceLog raceLog, Set<Competitor> competitors) {
        registerCompetitors(service.getServerAuthor(), raceLog, competitors,
                c -> RaceLogEventFactory.INSTANCE.createRegisterCompetitorEvent(MillisecondsTimePoint.now(),
                    service.getServerAuthor(), raceLog.getCurrentPassId(), c));
    }
    
    @Override
    public void registerCompetitors(RacingEventService service, RegattaLog regattaLog, Set<Competitor> competitors) {
        registerCompetitors(service.getServerAuthor(), regattaLog, competitors,
                c -> new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), service.getServerAuthor(),
                        MillisecondsTimePoint.now(), UUID.randomUUID(), c));
    }
    
    private MailService getMailService() {
        ServiceReference<MailService> ref = Activator.getContext()
                .getServiceReference(MailService.class);
        if (ref == null) {
            logger.warning("No file storage management service registered");
            return null;
        }
        return Activator.getContext().getService(ref);
    }
    
    @Override
    public void inviteCompetitorsForTrackingViaEmail(Event event, Leaderboard leaderboard,
            String serverUrlWithoutTrailingSlash, Set<Competitor> competitors, Locale locale) throws MailException {
        StringBuilder occuredExceptions = new StringBuilder();

        for (Competitor competitor : competitors){
            final String toAddress = competitor.getEmail();
            if (toAddress != null) {
                String leaderboardName = leaderboard.getName();
                String competitorName = competitor.getName();

                String url = DeviceMappingConstants.getDeviceMappingForRegattaLogUrl(serverUrlWithoutTrailingSlash,
                        event.getId().toString(), leaderboardName, DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING,
                        competitor.getId().toString(), NonGwtUrlHelper.INSTANCE);
                String subject = String.format("%s %s",
                        RaceLogTrackingI18n.STRING_MESSAGES.get(locale, "trackingInvitationFor"), competitorName);

                // taken from http://www.tutorialspoint.com/javamail_api/javamail_api_send_inlineimage_in_email.htm
                BodyPart messageTextPart = new MimeBodyPart();
                String htmlText = String.format("<h1>%s %s</h1>" + "<p>%s <b>%s</b></p>"
                        + "<img src=\"cid:image\" title=\"%s\"><br/>"
                        + "<a href=\"%s\">%s</a>",
                        RaceLogTrackingI18n.STRING_MESSAGES.get(locale, "welcomeTo"), leaderboardName,
                        RaceLogTrackingI18n.STRING_MESSAGES.get(locale, "scanQRCodeOrVisitUrlToRegisterAs"), competitorName,
                        url, url, RaceLogTrackingI18n.STRING_MESSAGES.get(locale, "alternativelyVisitThisLink"));
                
                try {
                    messageTextPart.setContent(htmlText, "text/html");

                    BodyPart messageImagePart = new MimeBodyPart();
                    InputStream imageIs = QRCodeGenerationUtil.create(url, 250);
                    DataSource imageDs = new ByteArrayDataSource(imageIs, "image/png");
                    messageImagePart.setDataHandler(new DataHandler(imageDs));
                    messageImagePart.setHeader("Content-ID", "<image>");

                    MimeMultipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageTextPart);
                    multipart.addBodyPart(messageImagePart);

                    getMailService().sendMail(toAddress, subject, multipart);
                } catch (MessagingException | MailException | WriterException | IOException e) {
                    logger.log(Level.SEVERE, "Error trying to send mail to " + competitor.getName()
                            + " with e-mail address " + toAddress, e);
                    occuredExceptions.append(e.getMessage()+"\r\n");
                }
            }
        }
        if (!(occuredExceptions.length() == 0)){
            throw new MailException(occuredExceptions.toString());
        }
    }
}
