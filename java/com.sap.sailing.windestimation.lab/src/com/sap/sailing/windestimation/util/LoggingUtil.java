package com.sap.sailing.windestimation.util;

import java.util.logging.Logger;

public class LoggingUtil {
    private static final Logger logger = Logger.getLogger(LoggingUtil.class.getName());

    public static synchronized void logInfo(String logMessage) {
        logger.info(logMessage);
    }
}
