package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.http.client.UrlBuilder;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.gwt.settings.UrlBuilderUtil;

/**
 * Manages all default settings of perspectives and components. This abstract implementation has no support for settings
 * storage. It is only capable of creating new default settings by means of {@link ComponentLifecycle} of the root
 * component managed by this {@link ComponentContext}. All in all, it is a dummy implementation for components which do
 * not have support for settings storage. If you need settings storage support, consider
 * {@link AbstractComponentContextWithSettingsStorage}.
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
    
    private final SettingsToUrlSerializer settingsToUrlSerializer = new SettingsToUrlSerializer();

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
    public UrlBuilder createUrlForSharingFromCurrentLocation(S settings, GenericSerializableSettings contextDefinition) {
        final UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        serializeSettingsToUrlBuilder(urlBuilder, settings, contextDefinition);
        return urlBuilder;
    }
    
    @Override
    public UrlBuilder createUrlForSharing(String path, S settings, GenericSerializableSettings contextDefinition) {
        final UrlBuilder urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        serializeSettingsToUrlBuilder(urlBuilder, settings, contextDefinition);
        return urlBuilder;
    }
    
    private void serializeSettingsToUrlBuilder(UrlBuilder urlBuilder, S settings, GenericSerializableSettings contextDefinition) {
        if(contextDefinition != null) {
            settingsToUrlSerializer.serializeToUrlBuilder(contextDefinition, urlBuilder);
        }
        if(settings instanceof SettingsMap) {
            settingsToUrlSerializer.serializeSettingsMapToUrlBuilder((SettingsMap) settings, urlBuilder);
        } else if(settings instanceof GenericSerializableSettings) {
            settingsToUrlSerializer.serializeToUrlBuilder((GenericSerializableSettings) settings, urlBuilder);
        }
    }
}
