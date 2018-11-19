package com.sap.sailing.windestimation.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingUtil {

    private static final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoggingUtil() {
    }

    public static int logInfo(String logMessage) {
        String logOutput = logTimeFormatter.format(LocalDateTime.now()) + " " + logMessage;
        System.out.println(logOutput);
        return logOutput.length() + 2;
    }

    public static void delete(int numberOfCharsToDelete) {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < numberOfCharsToDelete; i++) {
            str.append('\b');
        }
        System.out.print(str.toString());
    }

}
