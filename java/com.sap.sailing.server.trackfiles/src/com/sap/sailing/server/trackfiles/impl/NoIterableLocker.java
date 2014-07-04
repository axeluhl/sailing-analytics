package com.sap.sailing.server.trackfiles.impl;

class NoIterableLocker implements IterableLocker {
    @Override
    public void lock() {
    }

    @Override
    public void unlock() {
    }
}