package com.sap.sailing.windestimation.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingUtil {

    private static final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoggingUtil() {
    }

    public static void logInfo(String logMessage) {
        System.out.println(logTimeFormatter.format(LocalDateTime.now()) + " " + logMessage);
    }

}
