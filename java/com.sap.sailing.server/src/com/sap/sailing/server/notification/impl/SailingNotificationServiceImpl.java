package com.sap.sailing.server.notification.impl;

import java.net.MalformedURLException;
import java.net.URL;
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
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.impl.preferences.BoatClassResultsNotificationSet;
import com.sap.sailing.server.impl.preferences.BoatClassUpcomingRaceNotificationSet;
import com.sap.sailing.server.impl.preferences.CompetitorResultsNotificationSet;
import com.sap.sailing.server.notification.SailingNotificationService;
import com.sap.sse.common.Stoppable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.mail.queue.MailQueue;
import com.sap.sse.security.UserStore;

public class SailingNotificationServiceImpl implements Stoppable, SailingNotificationService {
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

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
                this.getClass().getClassLoader());
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

    private String createRaceBoardLink(TrackedRace trackedRace, Leaderboard leaderboard, Event event,
            LeaderboardGroup leaderboardGroup) {
        RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
        String link = getBaseURL(event).toString() + "/gwt/RaceBoard.html?eventId=" + event.getId() + "&leaderboardName=" + leaderboard.getName()
                + "&leaderboardGroupName=" + leaderboardGroup.getName() + "&raceName="
                + raceIdentifier.getRaceName() + "&showMapControls=true&viewShowNavigationPanel=true&regattaName="
                + raceIdentifier.getRegattaName();
        return link;
    }

    private String createHomeRacesListLink(Leaderboard leaderboard, Event event) {
        return createHomeRegattaLink("races", leaderboard, event);
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
    
    private String createHomeLeaderboardLink(Leaderboard leaderboard, Event event) {
        return createHomeRegattaLink("leaderboard", leaderboard, event);
    }
    
    private String createHomeRegattaLink(String tab, Leaderboard leaderboard, Event event) {
        String link = getBaseURL(event).toString() + "/gwt/Home.html#/regatta/" + tab + "/:eventId=" + event.getId() + "&regattaId="
                + leaderboard.getName();
        return link;
    }

    @Override
    public void notifyUserOnBoatClassRaceChangesStateToFinished(BoatClass boatClass, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            String link = createRaceBoardLink(trackedRace, leaderboard, event, leaderboardGroup);

            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
                @Override
                protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassRaceFinishedSubject", boatClass.getDisplayName());
                }

                @Override
                protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    return messages.get(locale, "boatClassRaceFinishedBody", boatClass.getDisplayName(),
                            raceDescription, encodeLink(link));
                }
            });
        });
    }

    @Override
    public void notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(BoatClass boatClass, Leaderboard leaderboard) {
        // TODO don't send notifications when a notification for the same boatClass / leaderboard has already been sent shortly before
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            String link = createHomeLeaderboardLink(leaderboard, event);

            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
                @Override
                protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassScoreCorrectionSubject", boatClass.getDisplayName());
                }

                @Override
                protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                    String leaderboardDescription = calculateLeaderboardDescription(locale, event, leaderboard);
                    return messages.get(locale, "boatClassScoreCorrectionBody", boatClass.getDisplayName(),
                            leaderboardDescription, encodeLink(link));
                }
            });
        });
    }

    public void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, TimePoint when) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            String link = createHomeRacesListLink(leaderboard, event);

            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassUpcomingRace) {
                @Override
                protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassUpcomingRaceSubject", boatClass.getDisplayName());
                }

                @Override
                protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    // TODO properly format time in the user's locale
                    String time = when.toString();
                    return messages.get(locale, "boatClassUpcomingRaceBody", boatClass.getDisplayName(),
                            raceDescription, time, encodeLink(link));
                }
            });
        });
    }


    private String encodeLink(String link) {
        String encodedLink = "<a href=\""+link+"\">"+link.replace("&", "&amp;")+"</a>";
        return encodedLink;
    }

    @Override
    public void notifyUserOnCompetitorPassesFinish(Competitor competitor, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            String link = createRaceBoardLink(trackedRace, leaderboard, event, leaderboardGroup);

            mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
                @Override
                protected String constructSubject(Competitor objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "competitorPassesFinishSubject", competitor.getName());
                }

                @Override
                protected String constructBody(Competitor objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    return messages.get(locale, "competitorPassesFinishBody", competitor.getName(), raceDescription,
                            encodeLink(link));
                }
            });
        });
    }

    @Override
    public void notifyUserOnCompetitorScoreCorrections(Competitor competitor, Leaderboard leaderboard) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            String link = createHomeLeaderboardLink(leaderboard, event);

            mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
                @Override
                protected String constructSubject(Competitor objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassScoreCorrectionSubject", competitor.getName());
                }

                @Override
                protected String constructBody(Competitor objectToNotifyAbout, Locale locale) {
                    String leaderboardDescription = calculateLeaderboardDescription(locale, event, leaderboard);
                    return messages.get(locale, "boatClassScoreCorrectionBody", competitor.getName(),
                            leaderboardDescription, encodeLink(link));
                }
            });
        });
    }
}
