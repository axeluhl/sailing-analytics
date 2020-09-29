package com.sap.sse.landscape.ssh;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.jcraft.jsch.Logger;

/**
 * Logging adapter from the JSch SSH framework to "JUL" (java.util.logging).
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class JCraftLogAdapter implements Logger {
    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(JCraftLogAdapter.class.getName());

    static Map<Integer, Level> levelMap = new HashMap<>();
    static {
        levelMap.put(com.jcraft.jsch.Logger.DEBUG, Level.FINE);
        levelMap.put(com.jcraft.jsch.Logger.ERROR, Level.SEVERE);
        levelMap.put(com.jcraft.jsch.Logger.FATAL, Level.SEVERE);
        levelMap.put(com.jcraft.jsch.Logger.INFO, Level.INFO);
        levelMap.put(com.jcraft.jsch.Logger.WARN, Level.WARNING);
    }

    @Override
    public void log(int level, String message) {
        logger.log(levelMap.get(level), message);
    }

    @Override
    public boolean isEnabled(int level) {
        return logger.isLoggable(levelMap.get(level));
    }

}
