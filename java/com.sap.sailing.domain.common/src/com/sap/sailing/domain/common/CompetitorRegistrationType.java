package com.sap.sailing.domain.common;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    // TODO Currently not supported -> implement later
    // OPEN_MODERATED(true, true),

    /** Competitors are registering themselves without the need of confirmation. */
    OPEN_UNMODERATED(true, false);

    private static final Logger logger = Logger.getLogger(CompetitorRegistrationType.class.getName());

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
     * Get ComptetitorRegistrationType for given name. If the name is unknown or null it returns {@link #CLOSED} as
     * default value.
     * 
     * @param name
     *            enum name
     * @param failForUnknown
     *            if set and name is not konwn competitor type than this method throws IllegalArgumentException
     * @return CompetitorRegistrationType
     */
    public static CompetitorRegistrationType valueOfOrDefault(String name, boolean failForUnknown) {
        if (name == null) {
            return CLOSED;
        } else {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException iae) {
                if (failForUnknown) {
                    throw iae;
                } else {
                    logger.log(Level.WARNING, "Unknown CompetitorRegistrationType " + name
                            + " for regatta. Interpreting it to default CLOSED.");
                    return CLOSED;
                }
            }

        }
    }

    /**
     * et ComptetitorRegistrationType for given name. If the name is unknown or null it returns {{@link #CLOSED} as
     * default value.
     * 
     * @param name
     *            enum name
     * @return CompetitorRegistrationType
     */
    public static CompetitorRegistrationType valueOfOrDefault(String name) {
        return valueOfOrDefault(name, false);
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
            // TODO activate when OPEN_MODERATED type is implemented
//        case OPEN_MODERATED:
//            return stringMessages.competitorRegistrationTypeOpenModerated();
        default:
            return this.name();
        }
    }
}
