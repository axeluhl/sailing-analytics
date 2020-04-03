package com.sap.sailing.gwt.server;

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
 * Use ${[variable name]} to get strings replaced within static pages.
 * These variables are available at the moment:
 * <table>
 *      <tr>
 *              <th>Variablename</th>
 *              <th>branded value</th>
 *              <th>debranded/whitelabeled</th>
 *      </tr>
 *      <tr>
 *              <td>SAP</td>
 *              <td>SAP&nbsp;</td>
 *              <td></td>
 *      </tr>
 *      <tr>
 *              <td>faviconPath</td>
 *              <td>images/whitelabel.ico</td>
 *              <td></td>
 *      </tr>
 *      <tr>
 *              <td>appiconPath</td>
 *              <td>images/sap-sailing-app-icon.png</td>
 *              <td></td>
 *      </tr>
 *      <tr>
 *              <td>debrandingActive</td>
 *              <td>false</td>
 *              <td>true</td>
 *      </tr>
 *      <tr>
 *              <td>saplogoBrowserInfo</td>
 *              <td>{@code <a class="sapLogo" href="http://www.sap.com"><img class="sapLogoImage" src="/images/logo-small@2x.png" alt="SAP Website"/></a>}</td>
 *              <td></td>
 *      </tr>
 * </table>
 * 
 * @author Georg Herdt
 *
 */
public class ClientConfigurationServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ClientConfigurationServlet.class.getName());
    
    /** serial version uid */
    private static final long serialVersionUID = -2228462977010198686L;

    public static final String DEBRANDING_PROPERTY_NAME = "com.sap.sailing.debranding";
    
    private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final boolean deBrandingActive = Boolean.valueOf(System.getProperty(DEBRANDING_PROPERTY_NAME, "false"));
        final String servletPath = req.getServletPath();
        byte[] cachedPage = null;
        if ((cachedPage = cache.get(generateKey(servletPath, deBrandingActive))) != null) {
            IOUtils.write(cachedPage, resp.getOutputStream());
            return;
        }
        try (InputStream in = this.getServletContext().getResourceAsStream(servletPath);) {
            byte[] buffer = IOUtils.toByteArray(in);

            String content = new String(buffer);
            for (Map.Entry<String, String> item : createReplacementMap(deBrandingActive).entrySet()) {
                content = content.replace("${" + item.getKey() + "}", item.getValue());
            }
            
            byte[] bytes = content.getBytes();
            IOUtils.write(bytes, resp.getOutputStream());
            cache.computeIfAbsent(generateKey(servletPath, deBrandingActive), key -> bytes);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "could not process or read resource " + servletPath, e);
            resp.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String generateKey(String servletPath, boolean active) {
        return servletPath + "_" + active;
    }

    private Map<String,String> createReplacementMap(boolean deBrandingActive) {
        final Map<String,String> map = new HashMap<>();
        final String title;
        final String faviconPath;
        final String appiconPath;
        final String saplogoBrowserInfo;
        if (deBrandingActive) {
            title = "";
            faviconPath = "images/whitelabel.ico";
            appiconPath = "images/sailing-app-icon.png";
            saplogoBrowserInfo = "";
        } else {
            title = "SAP ";
            faviconPath = "images/sap.ico";
            appiconPath = "images/sap-sailing-app-icon.png";
            saplogoBrowserInfo = "<a class=\"sapLogo\" href=\"http://www.sap.com\"><img class=\"sapLogoImage\" src=\"/images/logo-small@2x.png\" alt=\"SAP Website\"/></a>\r\n";
        } 
        map.put("SAP", title);
        map.put("faviconPath", faviconPath);
        map.put("appiconPath", appiconPath);
        map.put("debrandingActive", Boolean.toString(deBrandingActive));
        map.put("saplogoBrowserInfo", saplogoBrowserInfo);
        return map;
    }
}
