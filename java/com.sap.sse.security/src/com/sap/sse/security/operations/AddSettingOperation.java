package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class AddSettingOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = -6519495325480951824L;
    protected final String key;
    protected final Class<?> clazz;

    public AddSettingOperation(String key, Class<?> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAddSetting(key, clazz);
        return null;
    }
}
