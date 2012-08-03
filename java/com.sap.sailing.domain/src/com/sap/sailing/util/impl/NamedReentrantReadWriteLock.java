package com.sap.sailing.util.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.domain.common.Named;

public class NamedReentrantReadWriteLock extends ReentrantReadWriteLock implements Named {
    private static final long serialVersionUID = 2906084982209339774L;
    private final String name;
    
    public NamedReentrantReadWriteLock(String name, boolean fair) {
        super(fair);
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "ReentrantReadWriteLock "+getName()+" ("+(isFair()?"fair":"unfair")+")"; 
    }
}
