package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

/**
 * A perspective id and the perspective's own settings
 *
 * @param <PS>
 *            the perspective settings type
 * @author Frank Mittag
 */
public class PerspectiveIdAndSettings<PS extends Settings> implements Serializable {
    private static final long serialVersionUID = -6750964868964305325L;

    private final PS settings;
    
    public PerspectiveIdAndSettings(PS settings) {
        this.settings = settings;
    }

    public PS getSettings() {
        return settings;
    }
}