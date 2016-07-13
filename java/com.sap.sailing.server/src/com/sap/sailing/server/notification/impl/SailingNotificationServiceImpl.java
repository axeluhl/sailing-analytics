package com.sap.sailing.server.notification.impl;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;

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

public class SailingNotificationServiceImpl implements Stoppable, SailingNotificationService {
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

    private final Set<Stoppable> toStop = new HashSet<>();
    private final MailQueue mailQueue;
    private RacingEventService racingEventService;
    private final ResourceBundleStringMessagesImpl messages;

    private final BoatClassResultsNotificationSet boatClassResults;
    private final BoatClassUpcomingRaceNotificationSet boatClassUpcomingRace;
    private final CompetitorResultsNotificationSet competitorResults;

    public SailingNotificationServiceImpl(BundleContext bundleContext, MailQueue mailQueue) {
        this.mailQueue = mailQueue;
        this.messages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME,
                this.getClass().getClassLoader());

        toStop.add(boatClassResults = new BoatClassResultsNotificationSet(bundleContext));
        toStop.add(boatClassUpcomingRace = new BoatClassUpcomingRaceNotificationSet(bundleContext));
        toStop.add(competitorResults = new CompetitorResultsNotificationSet(bundleContext));
    }

    @Override
    public void stop() {
        toStop.forEach(Stoppable::stop);
    }

    public void setRacingEventService(RacingEventServiceImpl racingEventService) {
        this.racingEventService = racingEventService;
    }

    private Pair<Event, LeaderboardGroup> calculateAssociatedEventForLeaderboard(final Leaderboard leaderboard) {
        Set<Event> foundEvents = new HashSet<>();
        Set<LeaderboardGroup> foundLeaderboardGroups = new HashSet<>();
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
            return new Pair<>(Util.get(foundEvents, 0), Util.get(foundLeaderboardGroups, 0));
        } else if (foundEventsCount > 1 && foundLeaderboardGroups.size() == 1) {
            // could be a series
            LeaderboardGroup leaderboardGroup = Util.get(foundLeaderboardGroups, 0);
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
        }
        // No associated event found or association is ambiguous
        return null;
    }

    private void doWithEvent(Leaderboard leaderboard, BiConsumer<Event, LeaderboardGroup> consumer) {
        if (racingEventService == null) {
            // TODO log
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

    @Override
    public void notifyUserOnBoatClassRaceChangesStateToFinishing(BoatClass boatClass, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
            // TODO where to get the base URL?
            String link = "/gwt/RaceBoard.html?eventId=" + event.getId() + "&leaderboardName=" + leaderboard.getName()
                    + "&leaderboardGroupName=" + leaderboardGroup.getName() + "&raceName="
                    + raceIdentifier.getRaceName() + "&showMapControls=true&viewShowNavigationPanel=true&regattaName="
                    + raceIdentifier.getRegattaName();

            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
                @Override
                protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassRaceFinishingSubject", boatClass.getDisplayName());
                }

                @Override
                protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    return messages.get(locale, "boatClassRaceFinishingBody", boatClass.getDisplayName(),
                            raceDescription, link);
                }
            });
        });
    }

    @Override
    public void notifyUserOnBoatClassWhenScoreCorrectionsAreAvailable(BoatClass boatClass, Leaderboard leaderboard) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            // TODO where to get the base URL?
            String link = "/gwt/Home.html#/regatta/races/:eventId=" + event.getId() + "&regattaId="
                    + leaderboard.getName();

            mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
                @Override
                protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassScoreCorrectionSubject", boatClass.getDisplayName());
                }

                @Override
                protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                    String leaderboardDescription = calculateLeaderboardDescription(locale, event, leaderboard);
                    return messages.get(locale, "boatClassScoreCorrectionBody", boatClass.getDisplayName(),
                            leaderboardDescription, link);
                }
            });
        });
    }

    public void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, TimePoint when) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            // TODO where to get the base URL?
            String link = "/gwt/Home.html#/regatta/races/:eventId="+event.getId()+"&regattaId=" + leaderboard.getName();

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
                            raceDescription, time, link);
                }
            });
        });
    }

    @Override
    public void notifyUserOnCompetitorPassesFinish(Competitor competitor, TrackedRace trackedRace,
            Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
            // TODO where to get the base URL?
            String link = "/gwt/RaceBoard.html?eventId=" + event.getId() + "&leaderboardName=" + leaderboard.getName()
                    + "&leaderboardGroupName=" + leaderboardGroup.getName() + "&raceName="
                    + raceIdentifier.getRaceName() + "&showMapControls=true&viewShowNavigationPanel=true&regattaName="
                    + raceIdentifier.getRegattaName();

            mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
                @Override
                protected String constructSubject(Competitor objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "competitorPassesFinishSubject", competitor.getName());
                }

                @Override
                protected String constructBody(Competitor objectToNotifyAbout, Locale locale) {
                    String raceDescription = calculateRaceDescription(locale, event, leaderboard, raceColumn, fleet);
                    return messages.get(locale, "competitorPassesFinishBody", competitor.getName(), raceDescription,
                            link);
                }
            });
        });
    }

    @Override
    public void notifyUserOnCompetitorScoreCorrections(Competitor competitor, Leaderboard leaderboard) {
        doWithEvent(leaderboard, (event, leaderboardGroup) -> {
            // TODO where to get the base URL?
            String link = "/gwt/Home.html#/regatta/races/:eventId=" + event.getId() + "&regattaId="
                    + leaderboard.getName();

            mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
                @Override
                protected String constructSubject(Competitor objectToNotifyAbout, Locale locale) {
                    return messages.get(locale, "boatClassScoreCorrectionSubject", competitor.getName());
                }

                @Override
                protected String constructBody(Competitor objectToNotifyAbout, Locale locale) {
                    String leaderboardDescription = calculateLeaderboardDescription(locale, event, leaderboard);
                    return messages.get(locale, "boatClassScoreCorrectionBody", competitor.getName(),
                            leaderboardDescription, link);
                }
            });
        });
    }
}
