package com.sap.sse.security.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ext.Provider;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.jaxrs.RestServletContainer;
import com.sap.sse.common.HttpRequestHeaderConstants;
import com.sap.sse.security.SecurityService;

/**
 * Request filter to allow setting a divergent tenant group, for overriding the group used otherwise for created or
 * updated domain objects. The divergent tenant group is passed by its UUID on a request by the header value
 * <b>tenantGroupId</b>.
 *
 */
@Provider
public class DefaultTenantRequestFilter implements Filter {

    private static final Logger logger = Logger.getLogger(DefaultTenantRequestFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Checks the headers of a http request for an existing key <b>tenantGroupId<b>. If available the header value is
     * set as divergent default tenant group for this request.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final SecurityService securityService = getSecurityService(request.getServletContext());
        final String defaultTenantGroupId = httpRequest.getHeader(HttpRequestHeaderConstants.HEADER_KEY_DEFAULT_TENANT_GROUP_ID);
        if (defaultTenantGroupId != null) {
            securityService.setTemporaryDefaultTenant(UUID.fromString(defaultTenantGroupId));
            logger.info("executing request " + httpRequest.getRequestURI() + " with divergent tenant group "
                    + defaultTenantGroupId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if (defaultTenantGroupId != null) {
                securityService.setTemporaryDefaultTenant(null);
            }
        }
    }

    @Override
    public void destroy() {
    }

    private SecurityService getSecurityService(final ServletContext ctx) {
        @SuppressWarnings("unchecked")
        ServiceTracker<SecurityService, SecurityService> tracker = (ServiceTracker<SecurityService, SecurityService>) ctx
                .getAttribute(RestServletContainer.SECURITY_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
