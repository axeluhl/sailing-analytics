package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

/**
 * A perspective id and the perspective own settings
 *
 * @param <PS>
 *            the perspective settings type
 * @author Frank Mittag
 */
public class PerspectiveIdAndSettings<PS extends Settings> implements Serializable {
    private static final long serialVersionUID = -6750964868964305325L;

    private final Serializable perspectiveId;
    private final PS settings;
    
    public PerspectiveIdAndSettings(Serializable perspectiveId, PS settings) {
        this.perspectiveId = perspectiveId;
        this.settings = settings;
    }

    public Serializable getPerspectiveId() {
        return perspectiveId;
    }

    public PS getSettings() {
        return settings;
    }
}