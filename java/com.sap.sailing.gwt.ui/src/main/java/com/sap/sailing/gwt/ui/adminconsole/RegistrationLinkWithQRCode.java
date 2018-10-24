package com.sap.sailing.gwt.ui.adminconsole;

/**
 * Type to transfer data between {#link {@link RegistrationLinkWithQRCodeDialog) and caller.
 * 
 * @author Thomas Wiese
 *
 */
public class RegistrationLinkWithQRCode {

    /* Secret for extending registration URL. */
    private String secret;

    public RegistrationLinkWithQRCode() {
        super();
    }

    /**
     * Get entered secrect.
     * @return Secret or null, if not set
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Set secret.
     * @param secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

}
