package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * Manages all default settings of perspectives and components. This abstract implementation has no support for settings
 * storage. It is only capable of creating new default settings by means of {@link ComponentLifecycle} of the root
 * component managed by this {@link ComponentContext}. All in all, it is a dummy implementation for components which do
 * not have support for settings storage. If you need settings storage support, consider
 * {@link ComponentContextWithSettingsStorage}.
 * 
 * @author Vladislav Chumak
 *
 * @param <L>
 *            The {@link ComponentLifecycle} type of the root component/perspective containing all the settings for
 *            itself and its subcomponents
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public class SimpleComponentContext<L extends ComponentLifecycle<S, ?>, S extends Settings>
        implements ComponentContext<L, S> {

    protected final L rootLifecycle;

    /**
     * 
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     */
    public SimpleComponentContext(L rootLifecycle) {
        this.rootLifecycle = rootLifecycle;
    }

    /**
     * This operation is unsupported for this abstract implementation and will throw a
     * {@link UnsupportedOperationException} when it is called.
     */
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext "
                + this.getClass().getName() + " " + component.getPath() + " " + newDefaultSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public L getRootLifecycle() {
        return rootLifecycle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S getDefaultSettings() {
        return rootLifecycle.createDefaultSettings();
    }

    /**
     * This method returns always {@code false}, because it does not offer functionality for settings storage.
     */
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        return false;
    }
    
    @Override
    public void initInitialSettings(OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        onInitialSettingsLoaded.onSuccess(getDefaultSettings());
    }
}
