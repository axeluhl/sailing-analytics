package com.sap.sailing.domain.common.preferences;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;

/**
 * Preferences for Sailing users that define for which Objects a user wants to receive specific notifications.
 * This currently includes:
 * <ul>
 *   <li>Results for a favourite Competitor</li>
 *   <li>Results for a favourite BoatClass</li>
 *   <li>Upcoming races for a favourite BoatClass</li>
 * </ul>
 */
public class NotificationPreferences extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 8865010526321472376L;
    
    public static final String PREF_NAME = "sailing.notifications";
    
    private transient BoatClassNotificationPreferences boatClassPreferences;
    private transient CompetitorNotificationPreferences competitorPreferences;
    
    public NotificationPreferences() {
    }
    
    @Override
    protected void addChildSettings() {
        boatClassPreferences = new BoatClassNotificationPreferences("boatClassPreferences", this);
        competitorPreferences = new CompetitorNotificationPreferences("competitorPreferences", this);
    }
    
    public BoatClassNotificationPreferences getBoatClassPreferences() {
        return boatClassPreferences;
    }
    
    public CompetitorNotificationPreferences getCompetitorPreferences() {
        return competitorPreferences;
    }
}
