package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatChangeListener;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.RenamableImpl;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class BoatImpl extends RenamableImpl implements DynamicBoat {
    private static final long serialVersionUID = 3489730487528955788L;
    private final BoatClass boatClass;
    private final Serializable id;
    private String sailID;
    private Color color;
    private transient Set<BoatChangeListener> listeners;

    public BoatImpl(Serializable id, String name, BoatClass boatClass, String sailId) {
        this(id, name, boatClass, sailId, null);
    }

    public BoatImpl(Serializable id, String name, BoatClass boatClass, String sailID, Color color) {
        super(name);
        this.id = id;
        this.boatClass = boatClass;
        this.sailID = sailID;
        this.color = color;
        this.listeners = new HashSet<BoatChangeListener>();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        listeners = new HashSet<BoatChangeListener>();
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public String getSailID() {
        return sailID;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setName(String newName) {
        final String oldName = getName();
        if (!Util.equalsWithNull(oldName, newName)) {
            super.setName(newName);
            for (BoatChangeListener listener : getListeners()) {
                listener.nameChanged(oldName, newName);
            }
        }
    }

    @Override
    public void setSailId(String newSailId) {
        final String oldSailId = this.sailID;
        if (!Util.equalsWithNull(oldSailId, newSailId)) {
            this.sailID = newSailId;
            for (BoatChangeListener listener : getListeners()) {
                listener.sailIdChanged(oldSailId, newSailId);
            }
        }
    }

    @Override
    public void setColor(Color newColor) {
        final Color oldColor = this.color;
        if (!Util.equalsWithNull(oldColor, newColor)) {
            this.color = newColor;
            for (BoatChangeListener listener : getListeners()) {
                listener.colorChanged(oldColor, newColor);
            }
        }
    }

    @Override
    public Boat resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getOrCreateBoat(getId(), getName(), getBoatClass(), getSailID(), getColor());
    }

    @Override
    public void addBoatChangeListener(BoatChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeBoatChangeListener(BoatChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private Iterable<BoatChangeListener> getListeners() {
        synchronized (listeners) {
            return new HashSet<BoatChangeListener>(listeners);
        }
    }

    @Override
    public String toString() {
        return getName()==null?getSailID():getName();
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.BOAT;
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String... params) {
        return getTypeRelativeObjectIdentifier(this);
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(Boat boat) {
        return new TypeRelativeObjectIdentifier(boat.getId().toString());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(Serializable id) {
        return new TypeRelativeObjectIdentifier(id.toString());
    }
}
