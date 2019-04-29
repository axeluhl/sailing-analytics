package com.sap.sailing.server.notification.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.impl.preferences.BoatClassResultsNotificationSet;
import com.sap.sailing.server.impl.preferences.BoatClassUpcomingRaceNotificationSet;
import com.sap.sailing.server.impl.preferences.CompetitorResultsNotificationSet;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.notification.SailingNotificationService;
import com.sap.sse.common.Stoppable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.mail.queue.MailQueue;
import com.sap.sse.security.interfaces.UserStore;

public class SailingNotificationServiceImpl implements SailingNotificationService {
    public static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

    private static final Logger logger = Logger.getLogger(SailingNotificationServiceImpl.class.getName());

    private final Set<Stoppable> toStop = new HashSet<>();
    private final MailQueue mailQueue;
    private RacingEventService racingEventService;
    private final ResourceBundleStringMessagesImpl messages;

    private final BoatClassResultsNotificationSet boatClassResults;
    private final BoatClassUpcomingRaceNotificationSet boatClassUpcomingRace;
    private final CompetitorResultsNotificationSet competitorResults;
    private final URL defaultBaseURL;

    public SailingNotificationServiceImpl(BundleContext bundleContext, MailQueue mailQueue) throws MalformedURLException {
        this(mailQueue,
                new BoatClassResultsNotificationSet(bundleContext),
                new BoatClassUpcomingRaceNotificationSet(bundleContext),
                new CompetitorResultsNotificationSet(bundleContext));
    }
    
    /**
     * Constructor used for unit tests to not need BundelContext but directly work with a given UserStore.
     * @throws MalformedURLException 
     */
    public SailingNotificationServiceImpl(UserStore userStore, MailQueue mailQueue) throws MalformedURLException {
        this(mailQueue,
                new BoatClassResultsNotificationSet(userStore),
                new BoatClassUpcomingRaceNotificationSet(userStore),
                new CompetitorResultsNotificationSet(userStore));
    }

    public SailingNotificationServiceImpl(MailQueue mailQueue, BoatClassResultsNotificationSet boatClassResults,
            BoatClassUpcomingRaceNotificationSet boatClassUpcomingRace,
            CompetitorResultsNotificationSet competitorResults) throws MalformedURLException {
        this.mailQueue = mailQueue;
        this.messages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME,
                this.getClass().getClassLoader(), StandardCharsets.UTF_8.name());
        this.defaultBaseURL = new URL("https://www.sapsailing.com");
        
        toStop.add(this.boatClassResults = boatClassResults);
        toStop.add(this.boatClassUpcomingRace = boatClassUpcomingRace);
        toStop.add(this.competitorResults = competitorResults);
    }

    @Override
    public void stop() {
        toStop.forEach(Stoppable::stop);
    }

    public void setRacingEventService(RacingEventServiceImpl racingEventService) {
        this.racingEventService = racingEventService;
    }

    /**
     * Calculates the best matching {@link Event} and {@link LeaderboardGroup} for the given {@link Leaderboard}. If
     * there is just one Event/LeaderboardGroup, this pair is returned. If an event series is found and the associated
     * Event can be obtained, this combination is returned. The first Event of the series is used otherwise.
     */
    private Pair<Event, LeaderboardGroup> calculateAssociatedEventForLeaderboard(final Leaderboard leaderboard) {
        Set<Event> foundEvents = new LinkedHashSet<>();
        Set<LeaderboardGroup> foundLeaderboardGroups = new LinkedHashSet<>();
        racingEventService.getAllEvents().forEach(event -> {
            event.getLeaderboardGroups().forEach(leaderboardGroup -> {
                if (Util.contains(leaderboardGroup.getLeaderboards(), leaderboard)) {
                    foundEvents.add(event);
                    foundLeaderboardGroups.add(leaderboardGroup);
                }
            });
        });
        int foundEventsCount = foundEvents.size();
        if (foundEventsCount == 1) {
            // if multiple LeaderboardGroups of the single event reference the same leaderboard, we just use the
            // first LeaderboardGroup. This could be a non optimal match but helps to e.g. construct valid links.
            return new Pair<>(Util.get(foundEvents, 0), Util.get(foundLeaderboardGroups, 0));
        } else if (foundEventsCount > 1) {
            // could be a series
            for (final LeaderboardGroup leaderboardGroup : foundLeaderboardGroups) {
                if (leaderboardGroup.hasOverallLeaderboard()) {
                    CourseArea defaultCourseArea = leaderboard.getDefaultCourseArea();
                    if (defaultCourseArea != null) {
                        for (Event event : foundEvents) {
                            if (Util.contains(event.getVenue().getCourseAreas(), defaultCourseArea)) {
                                return new Pair<>(event, leaderboardGroup);
                            }
                        }
                    }
                }
                // Event <-> Leaderboard association is not set up correctly. The UI will show the Leaderboard for
                // multiple Events. So any of the found Events is ok for this case.
                return new Pair<>(Util.get(foundEvents, 0), Util.get(foundLeaderboardGroups, 0));
            }
        }
        // No associated event found or association is ambiguous
        return null;
    }

    /**
     * Calculated the best matching Event/LeaderBoardGroup pair via
     * {@link #calculateAssociatedEventForLeaderboard(Leaderboard)} and calls the given consumer with these instances.
     * If the racingEventService isn't already set, this will do nothing.
     */
    private void doWithEvent(Leaderboard leaderboard, BiConsumer<Event, LeaderboardGroup> consumer) {
        if (racingEventService == null) {
            logger.severe(
                    "Can't send notifications if " + getClass().getSimpleName() + ".racingEventService isn't set");
            return;
        }
        Pair<Event, LeaderboardGroup> eventAndLeaderboardGroup = calculateAssociatedEventForLeaderboard(leaderboard);
        if (eventAndLeaderboardGroup != null) {
            consumer.accept(eventAndLeaderboardGroup.getA(), eventAndLeaderboardGroup.getB());
        }
    }

    private String calculateRaceDescription(Locale locale, Event event, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet) {
        String raceName = raceColumn.getName();
        String eventName = event.getName();
        String leaderboardDisplayName = leaderboard.getDisplayName() != null ? leaderboard.getDisplayName()
                : leaderboard.getName();
        if (Util.size(raceColumn.getFleets()) > 1) {
            return messages.get(locale, "raceInFleetInRegattaOfEvent", raceName, leaderboardDisplayName, eventName,
                    fleet.getName());
        } else {
            return messages.get(locale, "raceInRegattaOfEvent", raceName, leaderboardDisplayName, eventName);
        }
    }

    private String calculateLeaderboardDescription(Locale locale, Event event, Leaderboard leaderboard) {
        String eventName = event.getName();
        String leaderboardDisplayName = leaderboard.getDisplayName() != null ? leaderboard.getDisplayName()
                : leaderboard.getName();
        return messages.get(locale, "leaderboardOfEvent", leaderboardDisplayName, eventName);
    }
    
    private Pair<String, String> createRaceBoardShowRaceLink(TrackedRace trackedRace, Leaderboard leaderboard,
            Event event, LeaderboardGroup leaderboardGroup, Locale locale) {
        return createRaceBoardLink(trackedRace, leaderboard, event, leaderboardGroup, "PLAYER",
                "raceboardShowRaceLinkTitle", locale);
    }

    private Pair<String, String> createRaceBoardRaceAnalysisLink(TrackedRace trackedRace, Leaderboard leaderboard,
            Event event, LeaderboardGroup leaderboardGroup, Locale locale) {
        return createRaceBoardLink(trackedRace, leaderboard, event, leaderboardGroup, "FULL_ANALYSIS",
                "raceboardRaceAnalysisLinkTitle", locale);
    }

    private Pair<String, String> createRaceBoardLink(TrackedRace trackedRace, Leaderboard leaderboard, Event event,
            LeaderboardGroup leaderboardGroup, String raceboardMode, String labelMessageKey, Locale locale) {
        RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
        String link = getBaseURL(event).toString() + "/gwt/RaceBoard.html?locale=" + locale.toLanguageTag()
        + "&eventId=" + event.getId() + "&leaderboardName=" + leaderboard.getName()
        + "&leaderboardGroupName=" + leaderboardGroup.getName() + "&raceName="
        + raceIdentifier.getRaceName() + "&showMapControls=true&regattaName="
        + raceIdentifier.getRegattaName() + "&mode=" + raceboardMode;
        return new Pair<String, String>(messages.get(locale, labelMessageKey), link);
    }
    
    private Pair<String, String> createHomeRacesListLink(Leaderboard leaderboard, Event event, Locale locale) {
        return createHomeRegattaLink("races", "racesOverviewLinkTitle", leaderboard, event, locale);
    }
    
    private Pair<String, String> createHomeLeaderboardLink(Leaderboard leaderboard, Event event,
            Locale locale) {
        return createHomeRegattaLink("leaderboard", "leaderboardShowResultsLinkTitle", leaderboard, event, locale);
    }
    
    private Pair<String, String> createHomeRegattaLink(String tab, String labelMessageKey, Leaderboard leaderboard,
            Event event, Locale locale) {
        String link = getBaseURL(event).toString() + "/gwt/Home.html?locale=" + locale.toLanguageTag()
                + "#/regatta/" + tab + "/:eventId=" + event.getId() + "&regattaId=" + leaderboard.getName();
        return new Pair<String, String>(messages.get(locale, labelMessageKey), link);
    }
    
    /**
     * The base URL for notifications as extracted from the {@link Event#getBaseURL() event}; defaults
     * to {@code https://www.sapsailing.com} if no base URL has been provided for the event.
     */
    private URL getBaseURL(final Event event) {
        final URL result;
        if (event.getBaseURL() == null) {
            result = defaultBaseURL;
        } else {
            result = event.getBaseURL();
        }
        return result;
    }

    @Override
    public void notifyUserOnBoatClassRaceChangesStateToFinished(BoatClass boatClass, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
                
                @Override
                protected NotificationMailTemplate getMailTemplate(BoatClass objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    return new NotificationMailTemplate(
                            messages.get(locale, "boatClassRaceFinishedSubject", boatClass.getDisplayName()), 
                            messages.get(locale, "boatClassRaceFinishedBody", boatClass.getDisplayName(),
                                    raceDescription),
                            getBaseURL(event),
                            createRaceBoardShowRaceLink(trackedRace, leaderboard, event, leaderboardGroup, locale),
                            createRaceBoardRaceAnalysisLink(trackedRace, leaderboard, event, leaderboardGroup, locale));
                }
            });
        });
    }

    @Override
    public void notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(BoatClass boatClass, Leaderboard leaderboard) {
        // TODO don't send notifications when a notification for the same boatClass / leaderboard has already been sent shortly before
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
                @Override
                protected NotificationMailTemplate getMailTemplate(BoatClass objectToNotifyAbout, Locale locale) {
                    String leaderboardDescription = calculateLeaderboardDescription(locale, event, leaderboard);
                    return new NotificationMailTemplate(
                            messages.get(locale, "boatClassScoreCorrectionSubject", boatClass.getDisplayName()), 
                            messages.get(locale, "boatClassScoreCorrectionBody", boatClass.getDisplayName(),
                                    leaderboardDescription),
                            getBaseURL(event),
                            createHomeLeaderboardLink(leaderboard, event, locale));
                }
            });
        });
    }

    public void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet,  TimePoint when) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassUpcomingRace) {
                @Override
                protected NotificationMailTemplate getMailTemplate(BoatClass objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    String time = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale)
                            .format(when.asDate());
                    return new NotificationMailTemplate(
                            messages.get(locale, "boatClassUpcomingRaceSubject", boatClass.getDisplayName()),
                            messages.get(locale, "boatClassUpcomingRaceBody", boatClass.getDisplayName(),
                                    raceDescription, time),
                            getBaseURL(event),
                            createHomeRacesListLink(leaderboard, event, locale));
                }
            });
        });
    }

    @Override
    public void notifyUserOnCompetitorPassesFinish(Competitor competitor, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
                @Override
                protected NotificationMailTemplate getMailTemplate(Competitor objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    return new NotificationMailTemplate(
                            messages.get(locale, "competitorPassesFinishSubject", competitor.getName()),
                            messages.get(locale, "competitorPassesFinishBody", competitor.getName(), raceDescription),
                            getBaseURL(event),
                            createRaceBoardShowRaceLink(trackedRace, leaderboard, event, leaderboardGroup, locale),
                            createRaceBoardRaceAnalysisLink(trackedRace, leaderboard, event, leaderboardGroup, locale));
                }
            });
        });
    }

    @Override
    public void notifyUserOnCompetitorScoreCorrections(Competitor competitor, Leaderboard leaderboard) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {

            mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
                @Override
                protected NotificationMailTemplate getMailTemplate(Competitor objectToNotifyAbout, Locale locale) {
                    String leaderboardDescription = calculateLeaderboardDescription(locale, event, leaderboard);
                    return new NotificationMailTemplate(
                            messages.get(locale, "competitorScoreCorrectionSubject", competitor.getName()),
                            messages.get(locale, "competitorScoreCorrectionBody", competitor.getName(),
                                    leaderboardDescription),
                            getBaseURL(event),
                            createHomeLeaderboardLink(leaderboard, event, locale));
                }
            });
        });
    }
}
