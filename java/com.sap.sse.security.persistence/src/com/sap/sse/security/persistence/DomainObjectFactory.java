package com.sap.sse.security.persistence;

import java.io.Serializable;

import org.apache.shiro.session.Session;

public interface DomainObjectFactory {

    Session loadSession(Serializable id);

}
