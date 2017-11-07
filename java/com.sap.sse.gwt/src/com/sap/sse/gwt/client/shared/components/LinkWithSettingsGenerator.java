package com.sap.sse.gwt.client.shared.components;

import java.util.function.Supplier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.UrlBuilder;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.gwt.settings.UrlBuilderUtil;

/**
 * EntryPoints based on {@link Component} or {@link Perspective} instances may use {@link ComponentContext} to handle
 * {@link Settings} related functionality such as loading settings and saving settings. This class helps creating links
 * to such an {@link EntryPoint} without the need of manually concatenating URL Parameters. Instead, the link is
 * constructed using the exact {@link Settings} instances used by the component.
 * 
 * @param <S>
 *            The type of Settings the target Component/Perspective uses.
 */
public class LinkWithSettingsGenerator<S extends Settings> {

    private final SettingsToUrlSerializer settingsToUrlSerializer = new SettingsToUrlSerializer();

    private final String baseUrl, path;
    private final GenericSerializableSettings[] contextDefinition;
    private final Supplier<S> settingsWithDefaultsFactory;
    
    /**
     * Constructs a link based on the current location's URL including the path.
     * 
     * @param contextDefinition
     *            {@link GenericSerializableSettings} instances that in sum define the context parameters of the
     *            {@link EntryPoint} the link is created for.
     */
    public LinkWithSettingsGenerator(GenericSerializableSettings... contextDefinition) {
        this(null, contextDefinition);
    }

    /**
     * Constructs a link based on the current location's URL but using the given path instead of the current path.
     * 
     * @param path
     *            the path to use for the generated link
     * @param contextDefinition
     *            {@link GenericSerializableSettings} instances that in sum define the context parameters of the
     *            {@link EntryPoint} the link is created for.
     */
    public LinkWithSettingsGenerator(String path, GenericSerializableSettings... contextDefinition) {
        this(null, path, contextDefinition);
    }
    
    /**
     * Constructs a link based on the current location's URL but using the given path instead of the current path.
     * 
     * @param path
     *            the path to use for the generated link
     * @param contextDefinition
     *            {@link GenericSerializableSettings} instances that in sum define the context parameters of the
     *            {@link EntryPoint} the link is created for.
     * @param settingsWithDefaultsFactory
     *            factory to create a settings instance with defaults correctly set. The created instance is used as a
     *            base for the serialization. This is needed in cases where defaults are dynamic, because it is not up
     *            to the dialog component to set the correct defaults for returned settings in case of dynamic defaults.
     */
    public LinkWithSettingsGenerator(String path, Supplier<S> settingsWithDefaultsFactory, GenericSerializableSettings... contextDefinition) {
        this(null, path, settingsWithDefaultsFactory, contextDefinition);
    }
    
    /**
     * Constructs a link based on the given base URL and path arguments.
     * 
     * @param baseUrl
     *            the base URL to use for the generated link
     * @param path
     *            the path to use for the generated link
     * @param contextDefinition
     *            {@link GenericSerializableSettings} instances that in sum define the context parameters of the
     *            {@link EntryPoint} the link is created for.
     */
    public LinkWithSettingsGenerator(String baseUrl, String path, GenericSerializableSettings... contextDefinition) {
        this(baseUrl, path, null, contextDefinition);
    }

    /**
     * Constructs a link based on the given base URL and path arguments.
     * 
     * @param baseUrl
     *            the base URL to use for the generated link
     * @param path
     *            the path to use for the generated link
     * @param contextDefinition
     *            {@link GenericSerializableSettings} instances that in sum define the context parameters of the
     *            {@link EntryPoint} the link is created for.
     * @param settingsWithDefaultsFactory
     *            factory to create a settings instance with defaults correctly set. The created instance is used as a
     *            base for the serialization. This is needed in cases where defaults are dynamic, because it is not up
     *            to the dialog component to set the correct defaults for returned settings in case of dynamic defaults.
     */
    public LinkWithSettingsGenerator(String baseUrl, String path, Supplier<S> settingsWithDefaultsFactory, GenericSerializableSettings... contextDefinition) {
        this.baseUrl = baseUrl;
        this.path = path;
        this.settingsWithDefaultsFactory = settingsWithDefaultsFactory;
        this.contextDefinition = contextDefinition;
    }

    /**
     * Creates a link using the contextDefinition (see constructors) but with no other {@link Settings}.
     */
    public String createUrl() {
        return createUrl(null);
    }
    
    /**
     * Creates a link using the contextDefinition (see constructors) and the given {@link Settings} instance.
     */
    public String createUrl(S settings) {
        final UrlBuilder urlBuilder;
        if (baseUrl != null && path != null) {
            urlBuilder = UrlBuilderUtil.createUrlBuilderFromBaseURLAndPathWithCleanParameters(baseUrl, path);
        } else if (path != null) {
            urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        } else {
            urlBuilder = UrlBuilderUtil.createUrlBuilderFromCurrentLocationWithCleanParameters();
        }
        final S settingsToSerialize;
        if (settingsWithDefaultsFactory == null) {
            settingsToSerialize = settings;
        } else {
            settingsToSerialize = settingsWithDefaultsFactory.get();
            SettingsUtil.copyValues(settings, settingsToSerialize);
        }
        serializeSettingsToUrlBuilder(urlBuilder, settingsToSerialize, contextDefinition);
        return urlBuilder.buildString();
    }

    /**
     * To be overwritten by subclasses to customize the link creation.
     */
    protected void serializeSettingsToUrlBuilder(UrlBuilder urlBuilder, S settings,
            GenericSerializableSettings... contextDefinition) {
        for(GenericSerializableSettings contextDefinitionItem : contextDefinition) {
            settingsToUrlSerializer.serializeToUrlBuilder(contextDefinitionItem, urlBuilder);
        }
        if (settings instanceof SettingsMap) {
            settingsToUrlSerializer.serializeSettingsMapToUrlBuilder((SettingsMap) settings, urlBuilder);
        } else if (settings instanceof GenericSerializableSettings) {
            settingsToUrlSerializer.serializeToUrlBuilder((GenericSerializableSettings) settings, urlBuilder);
        }
    }
}
