package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.UserActions;

public abstract class SubscriptionPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws IOException {
        final String username = getSubscriptionUserName(request);
        return !StringUtils.isEmpty(username) && super.isAccessAllowed(request, response,
                new String[] { SecuredSecurityTypes.USER.getStringPermissionForTypeRelativeIdentifier(UserActions.ADD_SUBSCRIPTION,
                        new TypeRelativeObjectIdentifier(username))});
    }

    protected abstract String getSubscriptionUserName(ServletRequest request);
}
