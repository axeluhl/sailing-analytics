package com.sap.sailing.server.notification;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public interface SailingNotificationService {

    void notifyUserOnBoatClassResults(BoatClass boatClass, String eventName, String regattaDisplayName, String link);

    void notifyUserOnBoatClassUpcomingRace(BoatClass boatClass, String eventName, String regattaDisplayName,
            TimePoint when, String link);

    void notifyUserOnCompetitorResults(Competitor competitor, String eventName, String regattaDisplayName, String link);

}