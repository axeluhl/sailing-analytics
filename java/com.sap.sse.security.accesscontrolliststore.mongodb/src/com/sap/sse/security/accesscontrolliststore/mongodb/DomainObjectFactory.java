package com.sap.sse.security.accesscontrolliststore.mongodb;

import com.sap.sse.security.AccessControlList;

public interface DomainObjectFactory {
    Iterable<AccessControlList> loadAllAccessControlLists();
    
    AccessControlList loadAccessControlList();
}
