package com.sap.sailing.gwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class ClientConfigurationServlet extends HttpServlet {

    /** serial version uid */
    private static final long serialVersionUID = -2228462977010198686L;
    
    private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        
        final String servletPath = req.getServletPath();
        byte[] value = null;
        if ((value = cache.get(servletPath)) != null) {
            IOUtils.write(value, resp.getOutputStream());
            return;
        }

        boolean deBrandingActive = Boolean.valueOf(System.getProperty("com.sap.sailing.debranding", "false"));
        if (req.getParameterMap().containsKey("whitelabel")) { // override with url parameter for testing usage only
            deBrandingActive = true;
        }        
        
        String title = "";
        String faviconPath = "images/whitelabel.ico";
        String appiconPath = "images/sailing-app-icon.png";
        if (!deBrandingActive) {
            title = "SAP ";
            faviconPath = "images/sap.ico";
            appiconPath = "images/sap-sailing-app-icon.png";
        }

        try (InputStream in = this.getServletContext().getResourceAsStream(servletPath);) {
            byte[] buffer = IOUtils.toByteArray(in);

            String content = new String(buffer);
            String replaced = content.replace("${SAP}", title).replace("${faviconPath}", faviconPath)
                    .replace("${appiconPath}", appiconPath)
                    .replace("${debrandingActive}", Boolean.toString(deBrandingActive));

            byte[] bytes = replaced.getBytes();
            IOUtils.write(bytes, resp.getOutputStream());
            cache.computeIfAbsent(servletPath, key -> bytes);
        }
    }

}
