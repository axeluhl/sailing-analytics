package com.sap.sailing.server.trackfiles.impl;

interface IterableLocker {
    void lock();

    void unlock();
}