package com.sap.sse.security;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.concurrent.ConcurrentWeakHashMap;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.util.TimerWithRunnable;

/**
 * Uses the {@link Cookie#ROOT_PATH} for the session ID cookie and delays the {@link #onChange(Session) onChange}
 * updates for calls to {@link #touch(SessionKey)} to avoid flooding the replication. It guarantees to notify the
 * {@link #touch(SessionKey)}-induced change at the latest after half the {@link Session#getTimeout() session expiration
 * interval}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SecurityWebSessionManager extends DefaultWebSessionManager {
    private static final Logger log = LoggerFactory.getLogger(SecurityWebSessionManager.class);
    private static final TimerWithRunnable timer = new TimerWithRunnable("Timer delaying Session.touch() onChange(s) notifications", /* isDaemon */ true);
    
    private static final Duration MAX_DURATION_ASSUMED_FOR_MESSAGE_DELIVERY = Duration.ONE_SECOND.times(30);
    
    /**
     * When {@link SecurityService#getSharedAcrossSubdomainsOf()} returns a non-{@code null}
     * domain, a different session cookie name is used to avoid collisions with more specific
     * default session cookies.
     */
    private static final String GLOBAL_SESSION_ID_COOKIE_NAME = "JSESSIONID_GLOBAL";
    
    /**
     * Wait no longer than this duration before pinging / bumping the session. The session timeout may be
     * very long, even compared to the average life span of a server instance. If the server instance dies
     * before having pinged / bumped the session, the ping/bump goes missing. If this continues to happen
     * over and over, the session may actually expire although the user had pinged/bumped it once or more.
     * Therefore, this duration has to be chosen such that in most cases it is shorter than the time a server
     * can be expected to survive from the point in time on when the ping/bump was received.
     */
    private static final Duration MAX_DURATION_AFTER_WHICH_TO_PING_SESSION = Duration.ONE_HOUR;
    
    private static final Object DUMMY = new Object(); // for use as dummy value in sessionsAlreadyScheduledForOnChange
    
    private static final ConcurrentWeakHashMap<Session, Object> sessionsAlreadyScheduledForOnChange = new ConcurrentWeakHashMap<>();
    
    public SecurityWebSessionManager() {
        super();
        getSessionIdCookie().setPath(Cookie.ROOT_PATH);
        final Thread backgroundThreadWaitingForSecurityServiceToObtainSharedAcrossSubdomains = new Thread(()->{
            final String domainForSecurityServiceSharing = Activator.getSecurityService().getSharedAcrossSubdomainsOf();
            if (domainForSecurityServiceSharing != null) {
                getSessionIdCookie().setDomain(domainForSecurityServiceSharing);
                getSessionIdCookie().setName(GLOBAL_SESSION_ID_COOKIE_NAME);
            }
        }, "Background thread of "+getClass().getName()+" waiting for security service");
        backgroundThreadWaitingForSecurityServiceToObtainSharedAcrossSubdomains.setDaemon(true);
        backgroundThreadWaitingForSecurityServiceToObtainSharedAcrossSubdomains.start();
    }

    @Override
    public void touch(SessionKey key) throws InvalidSessionException {
        Session s = lookupRequiredSession(key);
        s.touch();
        triggerOnChangeLatestInHalfTimeoutPeriod(s);
    }

    private void triggerOnChangeLatestInHalfTimeoutPeriod(Session s) {
        Duration sendDurationLatestIn = new MillisecondsDurationImpl(s.getTimeout()).minus(MAX_DURATION_ASSUMED_FOR_MESSAGE_DELIVERY);
        if (!sessionsAlreadyScheduledForOnChange.containsKey(s)) {
            sessionsAlreadyScheduledForOnChange.put(s, DUMMY);
            timer.schedule(()->{ onChange(s); sessionsAlreadyScheduledForOnChange.remove(s); },
                    MillisecondsTimePoint.now().plus(
                            sendDurationLatestIn.compareTo(MAX_DURATION_AFTER_WHICH_TO_PING_SESSION) > 0 ?
                                    MAX_DURATION_AFTER_WHICH_TO_PING_SESSION : sendDurationLatestIn).asDate());
        }
    }

    private Session lookupSession(SessionKey key) throws SessionException {
        if (key == null) {
            throw new NullPointerException("SessionKey argument cannot be null.");
        }
        return doGetSession(key);
    }

    private Session lookupRequiredSession(SessionKey key) throws SessionException {
        Session session = lookupSession(key);
        if (session == null) {
            String msg = "Unable to locate required Session instance based on SessionKey [" + key + "].";
            throw new UnknownSessionException(msg);
        }
        return session;
    }
    
    /**
     * Stores the Session's ID, usually as a Cookie, to associate with future requests.
     *
     * @param session the session that was just {@link #createSession created}.
     */
    @Override
    protected void onStart(Session session, SessionContext context) {
        //super.onStart(session, context);

        if (!WebUtils.isHttp(context)) {
            log.debug("SessionContext argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair. No session ID cookie will be set.");
            return;

        }
        HttpServletRequest request = WebUtils.getHttpRequest(context);
        HttpServletResponse response = WebUtils.getHttpResponse(context);

        if (isSessionIdCookieEnabled()) {
            Serializable sessionId = session.getId();
            storeSessionId(sessionId, request, response);
        } else {
            log.debug("Session ID cookie is disabled.  No cookie has been set for new session with id {}", session.getId());
        }

        request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE);
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
    }

    private void storeSessionId(Serializable currentId, HttpServletRequest request, HttpServletResponse response) {
        if (currentId == null) {
            String msg = "sessionId cannot be null when persisting for subsequent requests.";
            throw new IllegalArgumentException(msg);
        }
        Cookie template = getSessionIdCookie();
        Cookie cookie = new SamesiteCookie(template);
        String idString = currentId.toString();
        cookie.setValue(idString);
        cookie.setSecure(true);
        cookie.saveTo(request, response);
        log.trace("Set session ID cookie for session with id {}", idString);
    }
    
    
    private static class SamesiteCookie extends SimpleCookie {
        
        public SamesiteCookie(Cookie template) {
            super(template);
        }
        
        protected String buildHeaderValue(String name, String value, String comment, String domain, String path,
                int maxAge, int version, boolean secure, boolean httpOnly) {

            if (!StringUtils.hasText(name)) {
                throw new IllegalStateException("Cookie name cannot be null/empty.");
            }

            StringBuilder sb = new StringBuilder(name).append(NAME_VALUE_DELIMITER);

            if (StringUtils.hasText(value)) {
                sb.append(value);
            }

            appendComment(sb, comment);
            appendDomain(sb, domain);
            appendPath(sb, path);
            appendExpires(sb, maxAge);
            appendVersion(sb, version);
            appendSamesite(sb,"None");
            appendSecure(sb, secure);
            appendHttpOnly(sb, httpOnly);

            return sb.toString();

        }

        private void appendSamesite(StringBuilder sb, String value) {
            if (StringUtils.hasText(value)) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append("SameSite").append(NAME_VALUE_DELIMITER).append(value);
            }
        }
        
        private void appendComment(StringBuilder sb, String comment) {
            if (StringUtils.hasText(comment)) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(COMMENT_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(comment);
            }
        }

        private void appendDomain(StringBuilder sb, String domain) {
            if (StringUtils.hasText(domain)) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(DOMAIN_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(domain);
            }
        }

        private void appendPath(StringBuilder sb, String path) {
            if (StringUtils.hasText(path)) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(PATH_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(path);
            }
        }

        private void appendExpires(StringBuilder sb, int maxAge) {
            // if maxAge is negative, cookie should should expire when browser closes
            // Don't write the maxAge cookie value if it's negative - at least on Firefox it'll cause the 
            // cookie to be deleted immediately
            // Write the expires header used by older browsers, but may be unnecessary
            // and it is not by the spec, see http://www.faqs.org/rfcs/rfc2965.html
            // TODO consider completely removing the following 
            if (maxAge >= 0) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(MAXAGE_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(maxAge);
                sb.append(ATTRIBUTE_DELIMITER);
                Date expires;
                if (maxAge == 0) {
                    //delete the cookie by specifying a time in the past (1 day ago):
                    expires = new Date(System.currentTimeMillis() - DAY_MILLIS);
                } else {
                    //Value is in seconds.  So take 'now' and add that many seconds, and that's our expiration date:
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, maxAge);
                    expires = cal.getTime();
                }
                String formatted = toCookieDate(expires);
                sb.append(EXPIRES_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(formatted);
            }
        }

        private void appendVersion(StringBuilder sb, int version) {
            if (version > DEFAULT_VERSION) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(VERSION_ATTRIBUTE_NAME).append(NAME_VALUE_DELIMITER).append(version);
            }
        }

        private void appendSecure(StringBuilder sb, boolean secure) {
            if (secure) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(SECURE_ATTRIBUTE_NAME); //No value for this attribute
            }
        }

        private void appendHttpOnly(StringBuilder sb, boolean httpOnly) {
            if (httpOnly) {
                sb.append(ATTRIBUTE_DELIMITER);
                sb.append(HTTP_ONLY_ATTRIBUTE_NAME); //No value for this attribute
            }
        }
        
        private static String toCookieDate(Date date) {
            TimeZone tz = TimeZone.getTimeZone(GMT_TIME_ZONE_ID);
            DateFormat fmt = new SimpleDateFormat(COOKIE_DATE_FORMAT_STRING, Locale.US);
            fmt.setTimeZone(tz);
            return fmt.format(date);
        }
    }
}
