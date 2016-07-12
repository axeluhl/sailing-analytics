package com.sap.sailing.server.notification.impl;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.impl.preferences.BoatClassResultsNotificationSet;
import com.sap.sailing.server.impl.preferences.BoatClassUpcomingRaceNotificationSet;
import com.sap.sailing.server.impl.preferences.CompetitorResultsNotificationSet;
import com.sap.sailing.server.notification.SailingNotificationService;
import com.sap.sse.common.Stoppable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.mail.queue.MailQueue;

public class SailingNotificationServiceImpl implements Stoppable, SailingNotificationService {
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

    private final Set<Stoppable> toStop = new HashSet<>();
    private final MailQueue mailQueue;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.sap.sailing.server.notification.SailingNotificationService#notifyUserOnBoatClassResults(com.sap.sailing.
     * domain.base.BoatClass, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void notifyUserOnBoatClassResults(BoatClass boatClass, String eventName, String regattaDisplayName,
            String link) {
        mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassResults) {
            @Override
            protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                return messages.get(locale, "boatClassResultsSubject", boatClass.getDisplayName(), regattaDisplayName,
                        eventName);
            }

            @Override
            protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                return messages.get(locale, "boatClassResultsBody", boatClass.getDisplayName(), regattaDisplayName,
                        eventName);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sap.sailing.server.notification.SailingNotificationService#notifyUserOnBoatClassUpcomingRace(com.sap.sailing.
     * domain.base.BoatClass, java.lang.String, java.lang.String, com.sap.sse.common.TimePoint, java.lang.String)
     */
    @Override
    public void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, String eventName, String regattaDisplayName,
            TimePoint when, String link) {
        mailQueue.addNotification(new NotificationSetNotification<BoatClass>(boatClass, boatClassUpcomingRace) {
            @Override
            protected String constructSubject(BoatClass objectToNotifyAbout, Locale locale) {
                return messages.get(locale, "boatClassUpcomingRaceSubject", boatClass.getDisplayName(),
                        regattaDisplayName, eventName);
            }

            @Override
            protected String constructBody(BoatClass objectToNotifyAbout, Locale locale) {
                // TODO properly format time in the user's locale
                String time = when.toString();
                return messages.get(locale, "boatClassUpcomingRaceBody", boatClass.getDisplayName(), regattaDisplayName,
                        eventName, time);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sap.sailing.server.notification.SailingNotificationService#notifyUserOnCompetitorResults(com.sap.sailing.
     * domain.base.Competitor, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void notifyUserOnCompetitorResults(Competitor competitor, String eventName, String regattaDisplayName,
            String link) {
        mailQueue.addNotification(new NotificationSetNotification<Competitor>(competitor, competitorResults) {
            @Override
            protected String constructSubject(Competitor objectToNotifyAbout, Locale locale) {
                return messages.get(locale, "competitorResultsSubject", competitor.getName(), regattaDisplayName,
                        eventName);
            }

            @Override
            protected String constructBody(Competitor objectToNotifyAbout, Locale locale) {
                return messages.get(locale, "competitorResultsBody", competitor.getName(), regattaDisplayName,
                        eventName);
            }
        });
    }
}
