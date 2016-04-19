package com.sap.sse.common.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.common.settings.value.SettingsValue;

public abstract class SerializableSettings extends AbstractSettings implements Serializable {

    private static final long serialVersionUID = -2710627501473421333L;

    public SerializableSettings() {
        super();
    }

    public SerializableSettings(String name, AbstractSettings settings) {
        super(name, settings);
    }
    
    @GwtIncompatible
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        adoptValue((SettingsValue) ois.readObject());
        addChildSettingsInternal();
    }
    
    @GwtIncompatible
    private void writeObject(ObjectOutputStream out) throws IOException {
        SettingsValue innerValueObject = getInnerValueObject();
        out.writeObject(innerValueObject);
    }
}
