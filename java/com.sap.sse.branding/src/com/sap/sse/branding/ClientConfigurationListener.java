package com.sap.sse.branding;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import com.sap.sse.branding.BrandingConfigurationService.BrandingConfigurationProperty;
import com.sap.sse.branding.impl.Activator;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.rest.GwtLocaleFromHttpRequestUtil;

/**
 * JSP servlet is registered on *.html within web.xml. Use the following JSP expression 
 * <pre>${clientConfigurationContext['variableName']}</pre> to get strings replaced within the page. Among others, the
 * variables listed below are available for replacements:
 * <table border="1">
 * <tr>
 * <th>Variablename</th>
 * <th>branded value</th>
 * <th>debranded/whitelabeled</th>
 * </tr>
 * <tr>
 * <td>"brandTitle"</td>
 * <td>"SAP&nbsp;"</td>
 * <td>""</td>
 * </tr>
 * <tr>
 * <td>"debrandingActive"</td>
 * <td>"false"</td>
 * <td>"true"</td>
 * </tr>
 * <tr>
 * <td>"whitelabeled"</td>
 * <td>""</td>
 * <td>"-whitelabeled"</td>
 * </tr>
 * </table>
 * <p>
 * For a full list of variables, see the {@link BrandingConfigurationService.BrandingConfigurationProperty} enumeration type.<p>
 * 
 * Register a the jsp servlet for all the URLs that produce such static pages that you'd like to run replacements on. Example
 * registration in a {@code web.xml} configuration file:
 * 
 * <pre>
 *   &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;jsp&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;*.html&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 * <p>
 * 
 * 
 * @see com.sap.sailing.server.gateway.test.support.WhitelabelSwitchServlet
 * @author Georg Herdt
 *
 */
public class ClientConfigurationListener implements javax.servlet.ServletRequestListener {
    /**
     * In the {@link ServletContext} maintains an attribute
     * {@link BrandingConfigurationService.LOCALES_JSP_PROPERTY_NAME} which is a map from a pair of locale (e.g. "en")
     * and branding ID (possibly {@code null} for a debranded configuration) to {@link String}-keyed maps of properties
     * that can be used in JSP EL expressions in those HTML pages. The map is initialized lazily, so the locale- and
     * branding configuration-specific properties are added the first time a request for that combination of locale and
     * branding configuration is seen here.
     * <p>
     * 
     * The properties are constructed using
     * {@link BrandingConfigurationService#getBrandingConfigurationPropertiesForJspContext(Optional)} with the locale as
     * argument, where the property names are derived from the {@link BrandingConfigurationProperty#getPropertyName()}
     * method which get appended to the {@link BrandingConfigurationService#JSP_PROPERTY_NAME_PREFIX} prefix.
     * <p>
     * 
     * For the current request, the properties map is then fetched from the {@link ServletContext} and set for the
     * {@code requestScope}. This way, the maps don't need to be created for every request.
     */
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        if (sre.getServletRequest().getScheme().startsWith("http")) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) sre.getServletRequest();
            final String path = httpServletRequest.getServletPath();
            // populate JSP scopes (applicationScope and requestScope) only for HTML pages or "/" requests
            if (path != null && (path.endsWith("/") || path.endsWith(".html"))) {
                final ServletContext servletContext = httpServletRequest.getServletContext();
                Optional<String> requestLocale = GwtLocaleFromHttpRequestUtil.getLocaleFromHttpRequest(httpServletRequest);
                final Map<String, Object> brandingProperties = getAndCacheBrandingConfigurationPropertiesFromServletContext(
                        servletContext, requestLocale, Activator.getDefaultBrandingConfigurationService());
                httpServletRequest.setAttribute(BrandingConfigurationService.JSP_PROPERTY_NAME_PREFIX, brandingProperties);
            }
        }
    }
    
    private Map<String, Object> getAndCacheBrandingConfigurationPropertiesFromServletContext(
            ServletContext servletContext, Optional<String> locale, BrandingConfigurationService brandingConfigurationService) {
        @SuppressWarnings("unchecked")
        ConcurrentMap<Pair<String, String>, Map<String, Object>> brandingProperties =
            (ConcurrentMap<Pair<String, String>, Map<String, Object>>) servletContext.getAttribute(BrandingConfigurationService.JSP_PROPERTIES_BY_LOCALE_AND_BRANDING_ID);
        if (brandingProperties == null) {
            brandingProperties = new ConcurrentHashMap<>();
            servletContext.setAttribute(BrandingConfigurationService.JSP_PROPERTIES_BY_LOCALE_AND_BRANDING_ID, brandingProperties);
        }
        final Pair<String, String> key = new Pair<>(locale.orElse(null),
                brandingConfigurationService.isBrandingActive() ? brandingConfigurationService.getActiveBrandingConfiguration().getId() : null);
        return brandingProperties.computeIfAbsent(key, k->computeBrandingConfigurationProperties(brandingConfigurationService, locale));
    }
    
    private Map<String, Object> computeBrandingConfigurationProperties(BrandingConfigurationService brandingConfigurationService, Optional<String> requestLocale) {
        final Map<String, Object> brandingProperties = new HashMap<>(); // no concurrency control needed within single request
        brandingConfigurationService.getBrandingConfigurationPropertiesForJspContext(requestLocale).forEach((k, v) -> {
            brandingProperties.put(k.getPropertyName(), v);
        });
        return brandingProperties;
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // intentionally left blank
    }
}
