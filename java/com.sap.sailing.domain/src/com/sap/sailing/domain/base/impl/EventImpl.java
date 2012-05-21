package com.sap.sailing.domain.base.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class EventImpl extends NamedImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;
    
    private final Set<Regatta> regattas;

    public EventImpl(String name) {
        super(name);
        this.regattas = new HashSet<Regatta>();
    }

    @Override
    public Iterable<Regatta> getRegattas() {
        return Collections.unmodifiableSet(regattas);
    }

    @Override
    public void addRegatta(Regatta regatta) {
        regattas.add(regatta);
    }

    @Override
    public void removeRegatta(Regatta regatta) {
        regattas.remove(regatta);
    }
    
}
