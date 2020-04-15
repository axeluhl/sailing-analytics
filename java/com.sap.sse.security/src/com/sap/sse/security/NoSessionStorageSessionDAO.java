package com.sap.sse.security;

import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.subject.Subject;

/**
 * A special {@link SubjectDAO} implementation that refuses to create/store a session
 * for any subject. Use this for REST resources expecting Bearer token-based authentication
 * which shouldn't trigger a session. In {@code shiro.ini} configure like this:<pre>
 * 
 *   subjectDAO = com.sap.sse.security.NoSessionStorageSessionDAO
 *   securityManager.subjectDAO = $subjectDAO
 * </pre>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class NoSessionStorageSessionDAO extends DefaultSubjectDAO {
    @Override
    protected boolean isSessionStorageEnabled(Subject subject) {
        return false;
    }
}
