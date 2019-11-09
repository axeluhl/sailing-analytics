package com.sap.sse.gwt.server;

import com.sap.sse.gwt.client.CrossDomainStorageConfigurationService;

public class CrossDomainStorageConfigurationServiceImpl extends ResultCachingProxiedRemoteServiceServlet
        implements CrossDomainStorageConfigurationService {
    private static final long serialVersionUID = -274157076047911567L;

    @Override
    public String getAcceptableCrossDomainStorageRequestOriginRegexp() {
        return System.getProperty(ACCEPTABLE_CROSS_DOMAIN_STORAGE_REQUEST_ORIGIN_REGEXP_PROPERTY_NAME,
                "^.*\\.sapsailing\\.com$");
    }
}
