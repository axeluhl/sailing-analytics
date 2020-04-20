package com.sap.sse.debranding;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

/**
 * Use ${[variable name]} to get strings replaced within static pages. No escape syntax is currently available. All occurrences of the variables listed below
 * that are found in the document will be replaced. The following variables are available at the moment:
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
 * Register as a servlet for all the URLs that produce such static pages that you'd like to run replacements on. Example
 * registration in a {@code web.xml} configuration file:
 * 
 * <pre>
 *   &lt;servlet&gt;
 *       &lt;display-name&gt;ClientConfigurationServlet&lt;/display-name&gt;
 *       &lt;servlet-name&gt;ClientConfigurationServlet&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;com.sap.sse.debranding.ClientConfigurationServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;ClientConfigurationServlet&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;*.html&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 * <p>
 * 
 * The servlet caches the results, both, for the branded as well as the unbranded/replaced contents.
 * 
 * @see com.sap.sailing.server.gateway.test.support.WhitelabelSwitchServlet
 * @author Georg Herdt
 *
 */
public class ClientConfigurationServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ClientConfigurationServlet.class.getName());
    
    /** serial version uid */
    private static final long serialVersionUID = -2228462977010198686L;

    public static final String DEBRANDING_PROPERTY_NAME = "com.sap.sse.debranding";
    
    private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final boolean deBrandingActive = Boolean.valueOf(System.getProperty(DEBRANDING_PROPERTY_NAME, "false"));
        final String servletPath = req.getServletPath();
        final byte[] cachedPage;
        final String pageKey = generateKey(servletPath, deBrandingActive);
        if ((cachedPage = cache.get(pageKey)) != null) {
            IOUtils.write(cachedPage, resp.getOutputStream());
        } else {
            try (InputStream in = this.getServletContext().getResourceAsStream(servletPath)) {
                byte[] buffer = IOUtils.toByteArray(in);
                String content = new String(buffer);
                for (Map.Entry<String, String> item : createReplacementMap(deBrandingActive).entrySet()) {
                    content = content.replace("${" + item.getKey() + "}", item.getValue());
                }
                byte[] bytes = content.getBytes();
                IOUtils.write(bytes, resp.getOutputStream());
                cache.computeIfAbsent(pageKey, key -> bytes);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "could not process or read resource " + servletPath, e);
                resp.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private String generateKey(String servletPath, boolean active) {
        return servletPath + "_" + active;
    }

    private Map<String,String> createReplacementMap(boolean deBrandingActive) {
        final Map<String,String> map = new HashMap<>();
        final String title;
        final String whitelabeled;
        if (deBrandingActive) {
            title = "";
            whitelabeled = "-whitelabeled";
        } else {
            title = "SAP ";
            whitelabeled = "";
        } 
        map.put("SAP", title);
        map.put("debrandingActive", Boolean.toString(deBrandingActive));
        map.put("whitelabeled", whitelabeled);
        return map;
    }
}
