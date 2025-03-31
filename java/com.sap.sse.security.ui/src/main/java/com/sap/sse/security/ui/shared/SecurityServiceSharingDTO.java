package com.sap.sse.security.ui.shared;

import java.io.Serializable;

import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

/**
 * A security service ({@code SecurityService}) can be operated in isolated or shared mode. When in shared mode, the
 * Shiro configuration will deliver session cookies with a shared parent domain so that it will be used regardless the
 * sub-domain that the user is currently navigating in. In isolated mode the session cookie's domain will be specific to
 * the server URL used to access the application.
 * <p>
 * 
 * Likewise, the {@link CrossDomainStorage} needs to be configured differently, depending on whether the service
 * operates in isolated or shared mode. When in isolated mode, the "origin" used to identify the application's local and
 * session storage is the URL used to access the application. When in shared mode, different sub-domains would lead to
 * different local storages, so instead a common origin from which the {@code /gwt-base/StorageMessaging.html} entry
 * point is available is used to manage the storage. See {@link CrossDomainStorage} for details.
 * <p>
 * 
 * This data transfer object describes the configuration provided by the server, available from the
 * {@link UserManagementServiceAsync#getSharingConfiguration(com.google.gwt.user.client.rpc.AsyncCallback)} method.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SecurityServiceSharingDTO implements Serializable {
    private static final long serialVersionUID = 264755515738862492L;
    private final String jSessionIdCookieDomain;
    private final String baseUrlForCrossDomainStorage;

    public SecurityServiceSharingDTO(String jSessionIdCookieDomain, String baseUrlForCrossDomainStorage) {
        super();
        this.jSessionIdCookieDomain = jSessionIdCookieDomain;
        this.baseUrlForCrossDomainStorage = baseUrlForCrossDomainStorage;
    }

    public String getjSessionIdCookieDomain() {
        return jSessionIdCookieDomain;
    }

    public String getBaseUrlForCrossDomainStorage() {
        return baseUrlForCrossDomainStorage;
    }
}
