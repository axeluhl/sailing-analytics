package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * Base class for all datamining* components. It creates the {@link StringMessages}.
 * 
 * @author Maximilian Groﬂ (D064866)
 *
 * @param <SettingsType>
 */
public abstract class AbstractDataMiningComponent<SettingsType extends Settings>
        extends AbstractComponent<SettingsType> {

    private final StringMessages stringMessages = StringMessages.INSTANCE;

    public AbstractDataMiningComponent(Component<?> parent, ComponentContext<?> componentContext) {
        super(parent, componentContext);
    }

    /**
     * Gets the {@link #stringMessages} of the datamining module.
     * 
     * @return the string messages.
     */
    protected StringMessages getDataMiningStringMessages() {
        return stringMessages;
    }
}
