package com.sap.sse.datamining.components.management;

import java.util.logging.Level;

public interface MemoryMonitor {

    void log(Level level, String message);
    void logInfo(String message);
    void logWarning(String message);
    void logSevere(String message);
    
    boolean isPaused();
    void pause();
    void unpause();

}
