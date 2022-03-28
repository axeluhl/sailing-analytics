package com.sap.sailing.sailti.resultimport;

import java.net.URL;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.NamedImpl;

public class RegattaResultDescriptor extends NamedImpl {
    private static final long serialVersionUID = -2984628929705851226L;
    private final String id;
    private final String className;
    private final URL xrrFinalUrl;
    private final TimePoint publishedAt;

    public RegattaResultDescriptor(String id,
            String name,
            String className,
            URL xrrFinalUrl,
            TimePoint publishedAt) {
        super(name);
        this.id = id;
        this.className = className;
        this.xrrFinalUrl = xrrFinalUrl;
        this.publishedAt = publishedAt;
    }

    public String getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public URL getXrrFinalUrl() {
        return xrrFinalUrl;
    }

    public TimePoint getPublishedAt() {
        return publishedAt;
    }
}
