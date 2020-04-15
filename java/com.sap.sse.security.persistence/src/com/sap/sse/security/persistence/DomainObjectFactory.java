package com.sap.sse.security.persistence;

import java.util.Map;
import java.util.Set;

import org.apache.shiro.session.Session;

public interface DomainObjectFactory {
    Map<String, Set<Session>> loadSessionsByCacheName();
}
