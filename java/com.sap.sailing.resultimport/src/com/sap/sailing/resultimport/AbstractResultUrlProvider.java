package com.sap.sailing.resultimport;

import java.net.URL;

import com.sap.sailing.domain.resultimport.ResultUrlProvider;

public abstract class AbstractResultUrlProvider implements ResultUrlProvider {
    private static final long serialVersionUID = 7136651992901096961L;
    private final ResultUrlRegistry resultUrlRegistry;
    
    public AbstractResultUrlProvider(ResultUrlRegistry resultUrlRegistry) {
        super();
        this.resultUrlRegistry = resultUrlRegistry;
    }

    protected ResultUrlRegistry getResultUrlRegistry() {
        return resultUrlRegistry;
    }

    @Override
    public Iterable<URL> getReadableUrls() {
        return getResultUrlRegistry().getReadableResultUrls(getName());
    }

    @Override
    public Iterable<URL> getAllUrls() {
        return getResultUrlRegistry().getAllResultUrls(getName());
    }
}
