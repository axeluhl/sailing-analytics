package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.i18n.CommonStringMessages;

/**
 * Types of registration of competitors to a regatta. Regattas can be <b>closed</b>, so only invited competitors are
 * allowed or <b>open</b>, which means competitors can register themselves.
 * 
 * @author Thomas Wiese
 *
 */
public enum CompetitorRegistrationType {

    /** Competitors are registered and invited by regatta owner. */
    CLOSED(false, false),

    /** Competitors are registering themselve, but the regatta owner has to confirm the registration. */
    OPEN_MODERATED(true, true),

    /** Competitors are registering themselves without the need of confirmation. */
    OPEN_UNMODERATED(true, false);

    private boolean open;
    private boolean moderated;

    private CompetitorRegistrationType(boolean open, boolean moderated) {
        this.open = open;
        this.moderated = moderated;
    }

    /**
     * Is registration open to self register of competitors?
     * 
     * @return true for self registration
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Is registration moderated?
     * 
     * @return true if moderated
     */
    public boolean isModerated() {
        return this.moderated;
    }

    /**
     * Get i18n label.
     * 
     * @return message string
     */
    public String getLabel(CommonStringMessages stringMessages) {
        switch (this) {
        case CLOSED:
            return stringMessages.competitorRegistrationTypeClosed();
        case OPEN_UNMODERATED:
            return stringMessages.competitorRegistrationTypeOpenUnmoderated();
        case OPEN_MODERATED:
            return stringMessages.competitorRegistrationTypeOpenModerated();
        default:
            return this.name();
        }
    }
}
