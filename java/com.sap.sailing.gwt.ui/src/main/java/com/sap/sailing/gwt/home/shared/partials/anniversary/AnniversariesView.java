package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for UI component showing anniversary information.
 */
public interface AnniversariesView extends IsWidget {

    /**
     * Clear all existing anniversary information.
     */
    void clearAnniversaries();

    /**
     * Add countdown information for an upcoming anniversary.
     * 
     * @param countdown
     *            number of races until anniversary
     * @param teaser
     *            teaser headerline for anniversary countdown
     * @param description
     *            describing text for anniversary countdown
     */
    void addCountdown(int countdown, String teaser, String description);

    /**
     * Add announcement information for a reached anniversary.
     * 
     * @param iconUrl
     *            URL of the icon to show
     * @param target
     *            the number of races which were the anniversary's target
     * @param teaser
     *            teaser headerline for anniversary announcement
     * @param description
     *            describing text for anniversary announcement
     * @param linkUrl
     *            URL to the RaceBoard show the anniversary race
     */
    void addAnnouncement(String iconUrl, int target, String teaser, String description, String linkUrl);
}
