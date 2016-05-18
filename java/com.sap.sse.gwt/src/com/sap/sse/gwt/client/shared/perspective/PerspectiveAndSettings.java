package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

/**
 * A perspective and it's perspective-specific settings
 *
 * @param <PS>
 *            the perspective settings type
 * @author Frank Mittag
 */
public class PerspectiveAndSettings<PS extends Settings> implements Serializable {
    private static final long serialVersionUID = -6750964868964305325L;

    private final Perspective<PS> perspective;
    private final PS settings;
    
    public PerspectiveAndSettings(Perspective<PS> perspective, PS settings) {
        this.perspective = perspective;
        this.settings = settings;
    }

    public Perspective<PS> getPerspective() {
        return perspective;
    }

    public PS getSettings() {
        return settings;
    }
}