package com.sap.sailing.gwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class ClientConfigurationServlet extends HttpServlet {

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
            
        }
    }

    private String generateKey(String servletPath, boolean active) {
        return servletPath + "_" + active;
    }

    private Map<String,String> createReplacementMap(boolean deBrandingActive) {
        Map<String,String> map = new HashMap<>();
        String title = "";
        String faviconPath = "images/whitelabel.ico";
        String appiconPath = "images/sailing-app-icon.png";
        String saplogoBrowserInfo = "";
        if (!deBrandingActive) {
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
