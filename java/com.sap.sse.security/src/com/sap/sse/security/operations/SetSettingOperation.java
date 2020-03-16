package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class SetSettingOperation implements OperationWithResult<ReplicableSecurityService, Boolean> {
    private static final long serialVersionUID = 1L;

    protected final String key;
    protected final Object setting;

    public SetSettingOperation(String key, Object setting) {
        this.key = key;
        this.setting = setting;
    }

    @Override
    public Boolean internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.internalSetSetting(key, setting);
    }
}
