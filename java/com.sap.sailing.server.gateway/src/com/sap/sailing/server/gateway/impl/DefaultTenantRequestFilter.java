package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.UUID;

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

import com.sap.sailing.server.gateway.jaxrs.RestServletContainer;
import com.sap.sse.security.SecurityService;

@Provider
public class DefaultTenantRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String defaultTenantGroupId = httpRequest.getHeader("tenantGroupId");
        setDefaultTenantForRequest(request.getServletContext(), defaultTenantGroupId);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private void setDefaultTenantForRequest(final ServletContext ctx, final String defaultTenantGroupId) {
        getSecurityService(ctx)
                .setTemporaryDefaultTenant(defaultTenantGroupId != null ? UUID.fromString(defaultTenantGroupId) : null);
    }

    private SecurityService getSecurityService(final ServletContext ctx) {
        @SuppressWarnings("unchecked")
        ServiceTracker<SecurityService, SecurityService> tracker = (ServiceTracker<SecurityService, SecurityService>) ctx
                .getAttribute(RestServletContainer.SECURITY_SERVICE_TRACKER_NAME);
        return tracker.getService();
    }
}
