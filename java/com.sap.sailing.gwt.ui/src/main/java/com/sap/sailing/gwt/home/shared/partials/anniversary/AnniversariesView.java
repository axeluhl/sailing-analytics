package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversariesDTO;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;

/**
 * Interface for UI component showing anniversary information.
 */
public interface AnniversariesView extends IsWidget, RefreshableWidget<AnniversariesDTO> {

    /**
     * Clear all existing anniversary information.
     */
    void clearAnniversaries();

    /**
     * Add an {@link AnniversaryCountdown item} which can hold countdown information for an upcoming anniversary.
     * 
     * @return the added {@link AnniversaryCountdown} instance
     */
    AnniversaryCountdown addCountdown();

    /**
     * Add an {@link AnniversaryAnnouncement item} which can hold announcement information for a reached anniversary.
     * 
     * @return the added {@link AnniversaryAnnouncement} instance
     */
    AnniversaryAnnouncement addAnnouncement();

    /**
     * Interface representing an anniversary item which can hold countdown information for an upcoming anniversary.
     */
    public interface AnniversaryCountdown {

        /**
         * @param count
         *            a formatted {@link String count} to show in anniversary item
         */
        void setCount(String count);

        /**
         * @param unit
         *            the <code>count</code>'s {@link String unit} to show in anniversary item
         */
        void setUnit(String unit);

        /**
         * @param teaser
         *            the teaser headerline to show in anniversary item
         */
        void setTeaser(String teaser);

        /**
         * @param desciption
         *            describing text to show in anniversary item
         */
        void setDescription(String desciption);

        /**
         * @param content
         *            the content to show in the legal notices popup
         */
        void setLegalNotice(IsWidget content);

    }

    /**
     * Interface representing an anniversary item which can hold announcement information for a reached anniversary.
     */
    public interface AnniversaryAnnouncement extends AnniversaryCountdown {

        /**
         * @param iconUrl
         *            URL of the icon to show in anniversary item
         */
        void setIconUrl(String iconUrl);

        /**
         * @param linkUrl
         *            URL where the link shown in anniversary item refers to
         */
        void setLinkUrl(String linkUrl);
    }
}
