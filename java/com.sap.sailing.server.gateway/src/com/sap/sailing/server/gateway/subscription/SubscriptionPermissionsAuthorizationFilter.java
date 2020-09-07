package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.UserActions;

public abstract class SubscriptionPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {
    private static final Logger logger = Logger.getLogger(SubscriptionPermissionsAuthorizationFilter.class.getName());

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws IOException {
        final String username = getSubscriptionUserName(request);
        final boolean isAllowed = !StringUtils.isEmpty(username) && super.isAccessAllowed(request, response,
                new String[] { SecuredSecurityTypes.USER.getStringPermissionForTypeRelativeIdentifier(
                        UserActions.ADD_SUBSCRIPTION, new TypeRelativeObjectIdentifier(username)) });
        if (!isAllowed) {
            logger.log(Level.INFO,
                    "Subscription webhook event is denied for user " + (username != null ? username : ""));
        }
        return isAllowed;
    }

    protected abstract String getSubscriptionUserName(ServletRequest request);
}
