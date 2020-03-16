package com.sap.sse.security.operations;

import java.util.Locale;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class UpdateUserPropertiesOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final String username;
    protected final String fullName;
    protected final String company;
    protected final Locale locale;

    public UpdateUserPropertiesOperation(String username, String fullName, String company, Locale locale) {
        this.username = username;
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUpdateUserProperties(username, fullName, company, locale);
        return null;
    }
}
