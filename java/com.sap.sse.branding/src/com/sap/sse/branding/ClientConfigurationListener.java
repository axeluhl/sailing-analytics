package com.sap.sse.branding;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import com.sap.sse.branding.impl.Activator;
/**
 * JSP servlet is registered on *.html within web.xml . Use the following JSP expression 
 * <pre>applicationScope['clientConfigurationContext.variableName']}</pre> to get strings replaced within the page. The
 * variables listed below are available for replacements:
 * <table border="1">
 * <tr>
 * <th>Variablename</th>
 * <th>branded value</th>
 * <th>debranded/whitelabeled</th>
 * </tr>
 * <tr>
 * <td>"SAP"</td>
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
            if (path != null && (path.endsWith("/") || path.endsWith(".html"))) {
                final ServletContext ctx = sre.getServletContext();
                final String ctxBrandingActive = (String) ctx.getAttribute(BrandingConfigurationService.JSP_PROPERTY_NAME_PREFIX+BrandingConfigurationService.BRANDING_ACTIVE_JSP_PROPERTY_NAME);
                final BrandingConfigurationService brandingConfigurationService = Activator.getDefaultBrandingConfigurationService();
                final boolean brandingActive = brandingConfigurationService.isBrandingActive();
                if (ctxBrandingActive == null || !Boolean.toString(brandingActive).equalsIgnoreCase(ctxBrandingActive)) {
                    createReplacementMap(brandingConfigurationService).forEach((k, v) -> {
                        ctx.setAttribute(BrandingConfigurationService.JSP_PROPERTY_NAME_PREFIX + k, v);
                    });
                }
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // intentionally left blank
    }

    private Map<String, String> createReplacementMap(BrandingConfigurationService brandingConfigurationService) {
        final Map<String, String> map = new HashMap<>();
        final String title;
        final String whitelabeled;
        if (brandingConfigurationService.isBrandingActive()) {
            title = "SAP ";
            whitelabeled = "";
        } else {
            title = "";
            whitelabeled = "-whitelabeled";
        }
        map.put(BrandingConfigurationService.BRAND_TITLE_WITH_TRAILING_SPACE_JSP_PROPERTY_NAME, title);
        map.put(BrandingConfigurationService.DEBRANDING_ACTIVE_JSP_PROPERTY_NAME, Boolean.toString(!brandingConfigurationService.isBrandingActive()));
        map.put(BrandingConfigurationService.BRANDING_ACTIVE_JSP_PROPERTY_NAME, Boolean.toString(brandingConfigurationService.isBrandingActive()));
        map.put(BrandingConfigurationService.DASH_WHITELABELED_JSP_PROPERTY_NAME, whitelabeled);
        return map;
    }
}
