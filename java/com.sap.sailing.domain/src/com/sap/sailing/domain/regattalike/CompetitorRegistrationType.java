package com.sap.sailing.domain.regattalike;

/**
 * Types of registration of competitors to a regatta.
 * Regattas can be <b>closed</b>, so only invited competitors are allowed or <b>open</b>, which means competitors can register  
 * @author Thomas
 *
 */
public enum CompetitorRegistrationType {

    /** Competitors are registered and invited by regatta owner. */
    CLOSED(false, false, "Closed"),
    
    /** Competitors are registering themselve, but the regatta owner has to confirm the registration. */
    OPEN_MODERATED(true, true, "OpenModerated"),
    
    /** Competitors are registering themselves without the need of confirmation. */
    OPEN_UNMODERATED(true, false, "OpenUnmoderated");
    
    private boolean open;
    private boolean moderated;
    private String labelPrefix;
    
    private CompetitorRegistrationType(boolean open, boolean moderated, String labelPrefix) {
        this.open = open;
        this.moderated = moderated;
        this.labelPrefix = labelPrefix;
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
    public String getLabel() {
        // return "competitorRegistrationType" + this.labelPrefix;
        return this.labelPrefix;
    }
    
}
