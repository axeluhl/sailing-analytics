package com.sap.sse.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.gwt.client.xdstorage.impl.LocalStorageDrivenByMessageEvents;

public interface CrossDomainStorageConfigurationService extends RemoteService {
    String ACCEPTABLE_CROSS_DOMAIN_STORAGE_REQUEST_ORIGIN_REGEXP_PROPERTY_NAME = "gwt.acceptableCrossDomainStorageRequestOriginRegexp";

    /**
     * Obtains a regular expression that is used to match against the origin of message requests
     * sent to the {@link LocalStorageDrivenByMessageEvents} storage implementation. Messages whose
     * origin is not matched by this regular expression are dropped. If {@code null} is returned,
     * the store will accept only origins matching {@code ^.*\.sapsailing\.com$}.
     */
    String getAcceptableCrossDomainStorageRequestOriginRegexp();
}
