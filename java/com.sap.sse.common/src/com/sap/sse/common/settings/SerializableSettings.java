package com.sap.sse.common.settings;

import java.io.Serializable;

/**
 * Super class for {@link Settings} that need to be Java-serializable or compatible with GWT-RPC.
 */
public abstract class SerializableSettings extends AbstractSettings implements Serializable {
    private static final long serialVersionUID = -2710627501473421333L;
}
