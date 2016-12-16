package com.sap.sse.security;

import com.sap.sse.common.NamedWithID;

public interface HasAccessControlList extends NamedWithID {
    AccessControlList getAccessControlList();
}