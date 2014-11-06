package com.sap.sse.security;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;

/**
 * Allows access if current user has at least one role of the specified list.
 * <br/>
 * Basically, it's the same as {@link RolesAuthorizationFilter} but using {@literal OR} instead
 * of {@literal AND} on the specified roles.<p>
 * 
 * This class has been copied from the stackoverflow post
 * <a href="http://stackoverflow.com/questions/14980703/apache-shiro-allowing-multiple-roles-to-access-a-url-not-working">
 * http://stackoverflow.com/questions/14980703/apache-shiro-allowing-multiple-roles-to-access-a-url-not-working</a>.
 *
 * @see RolesAuthorizationFilter
 * @author Andy Belsky
 */
public class AnyOfRolesFilter extends RolesAuthorizationFilter {

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response,
                                   Object mappedValue) throws IOException {

        final Subject subject = getSubject(request, response);
        final String[] rolesArray = (String[]) mappedValue;

        if (rolesArray == null || rolesArray.length == 0) {
            //no roles specified, so nothing to check - allow access.
            return true;
        }

        for (String roleName : rolesArray) {
            if (subject.hasRole(roleName)) {
                return true;
            }
        }

        return false;
    }
}