package com.sap.sse.security.datamining.data.impl;

import java.util.GregorianCalendar;

import org.apache.shiro.session.Session;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.data.HasSessionContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.shared.impl.User;

/**
 * Equality is based on {@link Session#getId() session ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SessionWithContext implements HasSessionContext {
    private final Session session;
    private final HasUserContext userContext;
    
    public SessionWithContext(SecurityService securityService, Session session, User user) {
        super();
        this.session = session;
        this.userContext = new HasUserContext() {
            @Override
            public User getUser() {
                return user;
            }
            
            @Override
            public SecurityService getSecurityService() {
                return securityService;
            }
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((session == null) ? 0 : session.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionWithContext other = (SessionWithContext) obj;
        if (session == null) {
            if (other.session != null)
                return false;
        } else if (!session.getId().equals(other.session.getId()))
            return false;
        return true;
    }

    @Override
    public HasUserContext getUserContext() {
        return userContext;
    }
    
    @Override
    public int getStartYear() {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(session.getStartTimestamp());
        return gregorianCalendar.get(GregorianCalendar.YEAR);
    }

    @Override
    public String getStartMonth() {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(session.getStartTimestamp());
        return String.format("%4d-%02d", gregorianCalendar.get(GregorianCalendar.YEAR), gregorianCalendar.get(GregorianCalendar.MONTH)+1);
    }

    @Override
    public String getExpiryMonth() {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(getExpiryTimePoint().asDate());
        return String.format("%4d-%02d", gregorianCalendar.get(GregorianCalendar.YEAR), gregorianCalendar.get(GregorianCalendar.MONTH)+1);
    }

    @Override
    public Duration getDurationSinceLastAccess() {
        return session == null ? null : TimePoint.of(session.getLastAccessTime()).until(TimePoint.now());
    }
    
    @Override
    public Duration getDurationUntilSessionExpiry() {
        return session == null ? null : TimePoint.now().until(getExpiryTimePoint());
    }

    private TimePoint getExpiryTimePoint() {
        return TimePoint.of(session.getLastAccessTime()).plus(Duration.ofMillis(session.getTimeout()));
    }
}
