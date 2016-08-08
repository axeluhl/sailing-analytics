package com.sap.sse.gwt.settings;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

public class UrlBuilderUtil {
    
    private static final Logger log = Logger.getLogger(UrlBuilderUtil.class.getName());

    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParameters() {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        for (String parameterName : Window.Location.getParameterMap().keySet()) {
            if (!"gwt.codesvr".equals(parameterName) && !"locale".equals(parameterName)) {
                urlBuilder.removeParameter(parameterName);
            }
        }
        urlBuilder.setHash(null);
        return urlBuilder;
    }

    public static UrlBuilder createUrlBuilderFromBaseURLAndPathWithCleanParameters(String baseUrl, String path) {
        final int colonIndex = baseUrl.indexOf(':');
        final String protocol = colonIndex >= 0 ? baseUrl.substring(0, colonIndex) : "http";

        final int doubleSlashIndex = baseUrl.indexOf("//");
        final String baseURLWithoutProtocol = doubleSlashIndex >= 0 ? baseUrl.substring(doubleSlashIndex + 2) : baseUrl;
        final int pathIndex = baseURLWithoutProtocol.indexOf('/');
        final String hostAndPort = pathIndex >= 0 ? baseURLWithoutProtocol.substring(0, pathIndex)
                : baseURLWithoutProtocol;
        final String pathPrefix = pathIndex >= 0 ? baseURLWithoutProtocol.substring(pathIndex + 1) : "";

        final int portColonIndex = hostAndPort.indexOf(':');
        final String host = portColonIndex >= 0 ? hostAndPort.substring(0, portColonIndex) : hostAndPort;
        int port = UrlBuilder.PORT_UNSPECIFIED;
        if (portColonIndex >= 0) {
            try {
                port = Integer.valueOf(hostAndPort.substring(portColonIndex + 1));
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error while parsing port for baseUrl: " + baseUrl, e);
            }
        }

        final StringBuilder pathBuilder = new StringBuilder(pathPrefix);
        if (pathPrefix.endsWith("/")) {
            pathBuilder.append(path.startsWith("/") ? path.substring(1) : path);
        } else {
            if (!path.startsWith("/")) {
                pathBuilder.append("/");
            }
            pathBuilder.append(path);
        }

        final UrlBuilder urlBuilder = createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        urlBuilder.setProtocol(protocol);
        urlBuilder.setHost(host);
        urlBuilder.setPath(pathBuilder.toString());
        urlBuilder.setPort(port);
        return urlBuilder;
    }

    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(String pathAndHash) {
        final UrlBuilder urlBuilder = createUrlBuilderFromCurrentLocationWithCleanParameters();
        String[] documentAndFragment = pathAndHash.split("#", 2);
        urlBuilder.setPath(documentAndFragment[0]);
        if (documentAndFragment.length > 1) {
            urlBuilder.setHash(documentAndFragment[1]);
        }
        return urlBuilder;
    }

    public static UrlBuilder createUrlBuilderWithPathAndParameters(String path, Map<String, String> parameters) {
        final UrlBuilder urlBuilder = createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlBuilder.setParameter(entry.getKey(), entry.getValue());
        }
        return urlBuilder;
    }
}
