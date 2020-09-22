package com.sap.sailing.server.gateway.subscription;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.UserActions;

/**
 * Abstract Shiro authorization filter class for checking subscription adding/updating permission. This filter will
 * allow only user having permission <code>&quot;user:add_subscription:{username}&quot;</code> to access the system
 */
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
            logger.warning(() -> "Subscription webhook event is denied for user " + (username != null ? username : ""));
        }
        return isAllowed;
    }

    /**
     * Return subscription user name from request, this result then will be used to check if authenticated user has
     * permission to change subscription data for the user.
     */
    protected abstract String getSubscriptionUserName(ServletRequest request);
}
