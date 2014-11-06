package com.sap.sailing.domain.base.impl;

import java.net.URL;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sse.common.impl.NamedImpl;

public class RemoteSailingServerReferenceImpl extends NamedImpl implements RemoteSailingServerReference {
    private static final long serialVersionUID = 3561284977118738878L;

    /** the URL of the server */
    private final URL url;
    
    public RemoteSailingServerReferenceImpl(String name, URL url) {
        super(name);
        this.url = url;
    }

    @Override
    public URL getURL() {
        return url;
    }
}