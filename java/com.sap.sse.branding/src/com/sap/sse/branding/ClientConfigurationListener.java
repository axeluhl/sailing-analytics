package com.sap.sse.branding;

import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import com.sap.sse.branding.BrandingConfigurationService.BrandingConfigurationProperty;
import com.sap.sse.branding.impl.Activator;

/**
 * JSP servlet is registered on *.html within web.xml. Use the following JSP expression 
 * <pre>applicationScope['clientConfigurationContext.variableName']}</pre> to get strings replaced within the page. Among others, the
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
 * TODO bug6060 some String properties may depend on the locale; should we use a map keyed by the locale? How to index that map in the JSP expressions?
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
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        if (sre.getServletRequest().getScheme().startsWith("http")) {
            final String path = ((HttpServletRequest) sre.getServletRequest()).getServletPath();
            final Map<String, String[]> parameterMap = ((HttpServletRequest) sre.getServletRequest()).getParameterMap();
            final Optional<String> locale;
            if (parameterMap != null && parameterMap.containsKey("locale")) {
                locale = Optional.of(parameterMap.get("locale")[0]);
            } else {
                locale = Optional.empty();
            }
            if (path != null && (path.endsWith("/") || path.endsWith(".html"))) {
                final ServletContext ctx = sre.getServletContext();
                final Boolean ctxBrandingActive = (Boolean) ctx.getAttribute(
                        BrandingConfigurationService.JSP_PROPERTY_NAME_PREFIX+BrandingConfigurationProperty.BRANDING_ACTIVE_JSP_PROPERTY_NAME.getPropertyName());
                final BrandingConfigurationService brandingConfigurationService = Activator.getDefaultBrandingConfigurationService();
                final boolean brandingActive = brandingConfigurationService.isBrandingActive();
                if (ctxBrandingActive == null || brandingActive != ctxBrandingActive.booleanValue()) { // FIXME now we also need to check the locale
                    brandingConfigurationService.getBrandingConfigurationPropertiesForJspContext(locale).forEach((k, v) -> {
                        ctx.setAttribute(BrandingConfigurationService.JSP_PROPERTY_NAME_PREFIX + k.getPropertyName(), v);
                    });
                }
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // intentionally left blank
    }
}
