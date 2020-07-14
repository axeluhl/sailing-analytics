package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

public abstract class SubscriptionPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws IOException {
        String username = getSubscriptionUserName(request);
        if (StringUtils.isEmpty(username)) {
            return false;
        }

        return super.isAccessAllowed(request, response, new String[] { "USER:ADD_SUBSCRIPTION:" + username });
    }

    protected abstract String getSubscriptionUserName(ServletRequest request);
}
