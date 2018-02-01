package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.NationalityChangeListener;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class PersonImpl extends NamedImpl implements DynamicPerson {
    private static final long serialVersionUID = -2104903799224233508L;
    private Nationality nationality;
    private final Date dateOfBirth;
    private final String description;
    private transient Set<NationalityChangeListener> listeners;
    
    public PersonImpl(String name, Nationality nationality, Date dateOfBirth, String description) {
        super(name);
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.description = description;
        this.listeners = new HashSet<NationalityChangeListener>();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        listeners = new HashSet<NationalityChangeListener>();
    }
    
    @Override
    public Nationality getNationality() {
        return nationality;
    }
    
    @Override
    public void setNationality(Nationality newNationality) {
        final Nationality oldNationality = this.nationality;
        if (!Util.equalsWithNull(oldNationality, newNationality)) {
            this.nationality = newNationality;
            for (NationalityChangeListener listener : getListeners()) {
                listener.nationalityChanged(this, oldNationality, newNationality);
            }
        }
    }

    @Override
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void addNationalityChangeListener(NationalityChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeNationalityChangeListener(NationalityChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private Iterable<NationalityChangeListener> getListeners() {
        synchronized (listeners) {
            return new HashSet<NationalityChangeListener>(listeners);
        }
    }
}
