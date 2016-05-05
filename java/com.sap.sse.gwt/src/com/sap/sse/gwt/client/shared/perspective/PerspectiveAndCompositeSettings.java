package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;

/** 
 * A perspective and it's composite settings 
 * @param <PS>
 *      the {@link Perspective} settings type
 */
public class PerspectiveAndCompositeSettings<PS extends Settings> extends ComponentAndSettings<PerspectiveCompositeSettings<PS>>  {
    private static final long serialVersionUID = -5647140233314161466L;

    public PerspectiveAndCompositeSettings(Perspective<PS> perspective, PerspectiveCompositeSettings<PS> settings) {
        super(perspective, settings);
    }

    @Override
    public Perspective<PS> getComponent() {
        return (Perspective<PS>) super.getComponent();
    }
}