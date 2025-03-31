package com.sap.sse.security;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;

public class WebSubjectFactoryWithAnonymousPrincipalSupport extends DefaultWebSubjectFactory {
    @Override
    public Subject createSubject(SubjectContext context) {
        final PrincipalCollection principals = context.resolvePrincipals();
        if (principals == null || principals.isEmpty()) {
            // enable shiro to check permissions for non authenticated users by setting the explicitly modeled anonymous user
            context.setPrincipals(new SimplePrincipalCollection(SecurityService.ALL_USERNAME,
                    SecurityService.ALL_USERNAME));
        }
        return super.createSubject(context);
    }
}
