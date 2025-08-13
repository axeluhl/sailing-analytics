package com.sap.sse.branding;

/**
 * Describes all the aspects of branding as it may show in various places, specifically in the UI.
 * This includes whether branding is active, but also various logos to be displayed in different contexts,
 * as well as links to imprints and privacy statements.<p>
 * 
 * This is a service interface that can be implemented by different OSGi bundles, allowing for different
 * branding configurations. They may even be deployed at the same time and switched at runtime. If no branding
 * configuration is found or selected, no reference to any branding will be made ("whitelabeled" mode).<p>
 * 
 * For web pages to react to the branding configuration, JSP (Java Server Pages) can be used. Use {@link ClienctConfigurationListener}
 * in your web bundle's {@code web.xml} file as follows to get branding properties mapped into the JSP context:
 * <pre>
 *     &lt;listener&gt;
 *        &lt;listener-class>com.sap.sse.branding.ClientConfigurationListener&lt;/listener-class&gt;
 *     &lt;/listener&gt;
 * </pre>
 * Needless to say that your web bundle then must import the {@code com.sap.sse.branding} package.<p>
 * 
 * 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface BrandingConfigurationService {
    /**
     * Branding bundles have to specify an attribute with this name in their service registration for the
     * {@link BrandingConfigurationService} interface in the OSGi service registry. A branding implementation can then
     * be looked by by filtering for {@code (com.sap.sse.branding=SAP)} (assuming the value of this constant is
     * {@code "com.sap.sse.branding"} and you're looking for a branding implementation that identifies itself as "SAP").<p>
     * 
     * At the same time, this is the name of the system property that can be used to set a start-up branding
     * configuration for the local instance.
     */
    String BRANDING_ID_PROPERTY_NAME = "com.sap.sse.branding";
    
    /**
     * This is the prefix used for JSP properties that are set by the {@link ClientConfigurationListener}. To the value
     * of this constant, property names as defined, e.g., by {@link #BRAND_TITLE_WITH_TRAILING_SPACE_JSP_PROPERTY_NAME} or
     * {@link #BRANDING_ACTIVE_JSP_PROPERTY_NAME} are appended.
     */
    String JSP_PROPERTY_NAME_PREFIX = "clientConfigurationContext.";
    
    /**
     * The name of the JSP property, appended to the value of {@link #JSP_PROPERTY_NAME_PREFIX}, that contains the brand title
     * with a trailing space, so it can be concatenated with, e.g., the string "Sailing Analytics" to produce "{Your Brand Name} Sailing Analytics".
     */
    String BRAND_TITLE_WITH_TRAILING_SPACE_JSP_PROPERTY_NAME = "brandTitle";
    
    /**
     * The name of the JSP property, appended to the value of {@link #JSP_PROPERTY_NAME_PREFIX}, that indicates whether
     * debranding/whitelabeling is active. If this is {@code "true"}, the brand title will be empty, the property identified
     * by {@link #DASH_WHITELABELED_JSP_PROPERTY_NAME} will be {@code "-whitelabeled"}, and the property identified by
     * {@link #BRANDING_ACTIVE_JSP_PROPERTY_NAME} will be {@code "false"}. If this is {@code "false"}, the brand title will
     * be filled, the property identified by {@link #DASH_WHITELABELED_JSP_PROPERTY_NAME} will be empty, and the property
     * identified by {@link #BRANDING_ACTIVE_JSP_PROPERTY_NAME} will be {@code "true"}.
     */
    String DEBRANDING_ACTIVE_JSP_PROPERTY_NAME = "debrandingActive";
    
    /**
     * Opposite of {@link #DEBRANDING_ACTIVE_JSP_PROPERTY_NAME}.
     */
    String BRANDING_ACTIVE_JSP_PROPERTY_NAME = "brandingActive";
    
    /**
     * The name of the JSP property, appended to the value of {@link #JSP_PROPERTY_NAME_PREFIX}, whose value is
     * either empty (if branding is not active) or {@code "-whitelabeled"} (if branding is active). It may be used,
     * e.g., to produce an image URL that is different for whitelabeled and branded versions of the product.
     */
    String DASH_WHITELABELED_JSP_PROPERTY_NAME = "whitelabeled";
    
    boolean isBrandingActive();
    
    BrandingConfiguration getActiveBrandingConfiguration();
    
    /**
     * Looks for a {@link BrandingConfiguration} that was registered with the OSGi service registry
     * using the {@link BrandingConfigurationService#BRANDING_ID_PROPERTY_NAME} property with the value
     * as specified by the {@code brandingConfigurationId} parameter and sets it as the active configuration.
     * This will then be the configuration returned by {@link #getActiveBrandingConfiguration()}. If no such
     * configuration is found, branding is effectively deactivated and the {@link #isBrandingActive()} method
     * will return {@code false}.<p>
     * 
     * @return the branding configuration found under the given ID, or {@code null} if no such configuration
     * was found in the OSGi service registry. Note that in case {@code null} is returned and the branding
     * configuration with the ID specified is added to the registry only later, {@link #getActiveBrandingConfiguration()}
     * can still return that configuration if it is called after the configuration was added.<p>
     */
    BrandingConfiguration setActiveBrandingConfigurationById(String brandingConfigurationId);
}
