package com.sap.sse.gwt.settings;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;

/**
 * Helps to construct pre-parameterized {@link UrlBuilder} instances based on the current location/url. The created
 * {@link UrlBuilder} instances are constructed in a way so that "locale" and "gwt.codesvr" parameters are preserved,
 * but all other parameters as well as the fragment/hash part are being erased from the resulting UrlBuilder.
 */
public class UrlBuilderUtil {

    private static final Logger log = Logger.getLogger(UrlBuilderUtil.class.getName());

    /**
     * Creates an {@link UrlBuilder} based on the current location but with clean parameters and fragment/hash.
     * <p>
     * 
     * Example:
     * <ul>
     * <li>Current location: http://www.sapsailing.com/gwt/Home.html?locale=de&some_param=some_value#abc123</li>
     * <li>Returned UrlBuilder: http://www.sapsailing.com/gwt/Home.html?locale=de</li>
     * </ul>
     */
    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParameters() {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        for (String parameterName : Window.Location.getParameterMap().keySet()) {
            if (!"gwt.codesvr".equals(parameterName) && !LocaleInfo.getLocaleQueryParam().equals(parameterName)) {
                urlBuilder.removeParameter(parameterName);
            }
        }
        urlBuilder.setHash(null);
        return urlBuilder;
    }

    /**
     * Creates an {@link UrlBuilder} based on the current location but with clean parameters and fragment/hash. In
     * addition the base URL and path are being set specifically.
     * <p>
     * 
     * Example:
     * <ul>
     * <li>Current location: http://www.sapsailing.com/gwt/Home.html?locale=de&some_param=some_value#abc123</li>
     * <li>baseUrl: http://kielerwoche2016.sapsailing.com path: /gwt/Leaderboard.html</li>
     * <li>Returned UrlBuilder: http://kielerwoche2016.sapsailing.com/gwt/Leaderboard.html?locale=de</li>
     * </ul>
     */
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

        final UrlBuilder urlBuilder = createUrlBuilderFromCurrentLocationWithCleanParameters();
        urlBuilder.setProtocol(protocol);
        urlBuilder.setHost(host);
        urlBuilder.setPath(pathBuilder.toString());
        urlBuilder.setPort(port);
        return urlBuilder;
    }

    /**
     * Creates an {@link UrlBuilder} based on the current location but with clean parameters and fragment/hash. In
     * addition, the path part can specifically being set while the base (host/port/protocol) is the one of the current
     * location.
     * <p>
     * 
     * Example:
     * <ul>
     * <li>Current location: http://www.sapsailing.com/gwt/Home.html?locale=de&some_param=some_value#abc123</li>
     * <li>path: /gwt/SomePage.html</li>
     * <li>Returned UrlBuilder: http://www.sapsailing.com/gwt/SomePage.html?locale=de</li>
     * </ul>
     */
    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(String path) {
        return createUrlBuilderFromCurrentLocationWithCleanParameters().setPath(path);
    }

    /**
     * Creates an {@link UrlBuilder} based on the current location but with clean parameters and fragment/hash. In
     * addition, the path and hash parts can specifically being set while the base (host/port/protocol) is the one of
     * the current location.
     * <p>
     * 
     * Example:
     * <ul>
     * <li>Current location: http://www.sapsailing.com/gwt/Home.html?locale=de&some_param=some_value#abc123</li>
     * <li>path: /gwt/SomePage.html</li>
     * <li>hash: /some/place/:key=value</li>
     * <li>Returned UrlBuilder: http://www.sapsailing.com/gwt/SomePage.html?locale=de#/some/place/:key=value</li>
     * </ul>
     */
    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParametersAndPathAndHash(String path,
            String hash) {
        return createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path).setHash(hash);
    }

    /**
     * Creates an {@link UrlBuilder} based on the current location but with clean parameters and fragment/hash. In
     * addition, the path part as well as URL parameters can specifically being set while the base (host/port/protocol)
     * is the one of the current location.
     * <p>
     * 
     * Example:
     * <ul>
     * <li>Current location: http://www.sapsailing.com/gwt/Home.html?locale=de&some_param=some_value#abc123</li>
     * <li>path: /gwt/SomePage.html</li>
     * <li>parameters: {p1: [abc], p2: [xyz, 123]}</li>
     * <li>Returned UrlBuilder: http://www.sapsailing.com/gwt/SomePage.html?locale=de&p1=abc&p2=xyz&p2=123</li>
     * </ul>
     */
    public static UrlBuilder createUrlBuilderWithPathAndParameters(String path, Map<String, String> parameters) {
        final UrlBuilder urlBuilder = createUrlBuilderFromCurrentLocationWithCleanParametersAndPath(path);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlBuilder.setParameter(entry.getKey(), entry.getValue());
        }
        return urlBuilder;
    }

    /**
     * Creates an {@link UrlBuilder} based on the current location but with clean parameters and fragment/hash. In
     * addition, the path and hash parts as well as URL parameters can specifically being set while the base
     * (host/port/protocol) is the one of the current location.
     * <p>
     * 
     * Example:
     * <ul>
     * <li>Current location: http://www.sapsailing.com/gwt/Home.html?locale=de&some_param=some_value#abc123</li>
     * <li>path: /gwt/SomePage.html</li>
     * <li>hash: /some/place/:key=value parameters: {p1: [abc]}</li>
     * <li>Returned UrlBuilder: http://www.sapsailing.com/gwt/SomePage.html?locale=de&p1=abc#/some/place/:key=value</li>
     * </ul>
     */
    public static UrlBuilder createUrlBuilderWithPathAndHashAndParameters(String path, String hash,
            Map<String, String> parameters) {
        return createUrlBuilderWithPathAndParameters(path, parameters).setHash(hash);
    }
}
