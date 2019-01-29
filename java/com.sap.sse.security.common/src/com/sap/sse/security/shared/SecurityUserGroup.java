package com.sap.sse.security.shared;

import java.util.Map;
import java.util.UUID;

public interface SecurityUserGroup<RD extends RoleDefinition> {

    String getName();

    UUID getId();

    Map<RD, Boolean> getRoleDefinitionMap();

}