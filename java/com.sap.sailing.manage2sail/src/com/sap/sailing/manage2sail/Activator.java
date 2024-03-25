package com.sap.sailing.manage2sail;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.client.utils.URIBuilder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    private static final String MANAGE2SAIL_HOSTNAME = "www.manage2sail.com";
    private static final String ACCESS_TOKEN_PROPERTY_NAME = "manage2sail.accesstoken";
    private static final String ACCESS_TOKEN_URL_QUERY_PARAMETER_NAME = "accesstoken";
    private static Activator INSTANCE;
    private String accessToken;
    
    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY_NAME);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
    
    public static Activator getInstance() {
        if (INSTANCE == null) { // test scenario maybe
            INSTANCE = new Activator();
        }
        return INSTANCE;
    }
    
    private String getAccessToken() {
        return accessToken;
    }
    
    /**
     * If and only if the {@link URL#getHost() host} part of the URL equals the value of the {@link #getManage2SailHostname()} and
     * there is not yet a query parameter named {@code accesstoken} in the {@code url},
     * a new URL with an "accesstoken=${accessToken}" parameter appended to the {@link URL#getQuery() query} part of the {@code url}
     * will be returned. Otherwise, the {@code url} will be returned unchanged.
     */
    public URL addAccessTokenToManage2SailUrl(URL url) throws URISyntaxException, MalformedURLException {
        final URL result;
        final URIBuilder uriBuilder = new URIBuilder(url.toURI());
        final boolean containsAccessTokenQueryParam = uriBuilder.getQueryParams().stream().anyMatch(nameValuePair -> nameValuePair.getName().equals(ACCESS_TOKEN_URL_QUERY_PARAMETER_NAME));
        if (url.getHost().equals(getManage2SailHostname()) && !containsAccessTokenQueryParam) {
            result = uriBuilder.addParameter(ACCESS_TOKEN_URL_QUERY_PARAMETER_NAME, getAccessToken()).build().toURL();
        } else {
            result = url;
        }
        return result;
    }

    public String getManage2SailHostname() {
        return MANAGE2SAIL_HOSTNAME;
    }
}
