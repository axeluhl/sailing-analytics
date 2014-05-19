package com.sap.sailing.domain.base.impl;

import java.net.URL;

import com.sap.sailing.domain.base.SailingServer;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class SailingServerImpl extends NamedImpl implements SailingServer {
    private static final long serialVersionUID = 3561284977118738878L;

    /** the URL of the server */
    private URL url;

    public SailingServerImpl(String name, URL url) {
        super(name);
        this.url = url;
    }

    @Override
    public URL getURL() {
        return url;
    }
}
