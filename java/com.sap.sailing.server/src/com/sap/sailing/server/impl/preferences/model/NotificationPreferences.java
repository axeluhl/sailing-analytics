package com.sap.sailing.server.impl.preferences.model;

import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;

/**
 * Preferences for Sailing users that define for which Objects a user wants to receive specific notifications.
 * This currently includes:
 * <ul>
 *   <li>Results for a favorite Competitor</li>
 *   <li>Results for a favorite BoatClass</li>
 *   <li>Upcoming races for a favorite BoatClass</li>
 * </ul>
 */
public class NotificationPreferences extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 8865010526321472376L;
    
    public static final String PREF_NAME = "sailing.notifications";
    
    private transient BoatClassNotificationPreferences boatClassPreferences;
    private transient CompetitorNotificationPreferences competitorPreferences;
    
    public NotificationPreferences(RacingEventService racingEventService) {
        boatClassPreferences = new BoatClassNotificationPreferences("boatClassPreferences", this, racingEventService);
        competitorPreferences = new CompetitorNotificationPreferences("competitorPreferences", this, racingEventService);
    }
    
    @Override
    protected void addChildSettings() {
        // We do not create the Setting instances here, because access to the RacingEventService would not be given.
        // Doing this, Java/GWT Serialization isn't working anymore. Because the preferences are only serialized as JSON
        // in the backend an transferred as DTO to the frontend, this isn't a problem. Due to usage of BoatClass and
        // Competitor domain objects, it wouldn't be GWT compatible anyway.
        // The usage of Java Serialization isn't planned by now too.
    }
    
    public BoatClassNotificationPreferences getBoatClassPreferences() {
        return boatClassPreferences;
    }
    
    public CompetitorNotificationPreferences getCompetitorPreferences() {
        return competitorPreferences;
    }
}
