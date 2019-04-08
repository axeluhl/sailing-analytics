package com.sap.sailing.gwt.ui.datamining.presentation;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.ui.client.presentation.AbstractResultsPresenter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * Base class for all {@link AbstractResultsPresenter} implementations within the sailing domain specific module.
 * Provides the {@link #stringMessages} for these components.
 * 
 * @author Maximilian Groﬂ (D064866)
 *
 */
public abstract class AbstractSailingResultsPresenter<SettingsType extends Settings>
        extends AbstractResultsPresenter<SettingsType> {
    protected final StringMessages stringMessages;

    public AbstractSailingResultsPresenter(Component<?> parent, ComponentContext<?> context,
            StringMessages stringMessages) {
        super(parent, context);
        this.stringMessages = stringMessages;
    }
}
