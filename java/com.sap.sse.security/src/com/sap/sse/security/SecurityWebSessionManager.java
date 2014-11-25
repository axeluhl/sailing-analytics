package com.sap.sse.security;

import java.util.WeakHashMap;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimerWithRunnable;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
    private static final TimerWithRunnable timer = new TimerWithRunnable("Timer delaying Session.touch() onChange(s) notifications", /* isDaemon */ true);
    private static final Duration MAX_DURATION_ASSUMED_FOR_MESSAGE_DELIVERY = Duration.ONE_SECOND.times(30);
    
    private static final WeakHashMap<Session, Void> sessionsAlreadyScheduledForOnChange = new WeakHashMap<>();
    
    public SecurityWebSessionManager() {
        super();
        getSessionIdCookie().setPath(Cookie.ROOT_PATH);
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
            sessionsAlreadyScheduledForOnChange.put(s, null);
            timer.schedule(()->{ onChange(s); sessionsAlreadyScheduledForOnChange.remove(s); },
                    MillisecondsTimePoint.now().plus(sendDurationLatestIn).asDate());
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
}
