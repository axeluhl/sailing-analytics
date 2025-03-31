package com.sap.sse.security;

import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.subject.Subject;

/**
 * A special {@link SubjectDAO} implementation that refuses to create/store a session for a subject that is not
 * authenticated. In {@code shiro.ini} configure like this:
 * 
 * <pre>
 * 
 *   subjectDAO = com.sap.sse.security.NoSessionStorageForUnauthenticatedSessionsSessionDAO
 *   securityManager.subjectDAO = $subjectDAO
 * </pre>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class NoSessionStorageForUnauthenticatedSessionsSessionDAO extends DefaultSubjectDAO {
    @Override
    protected boolean isSessionStorageEnabled(Subject subject) {
        return subject.isAuthenticated();
    }
}
