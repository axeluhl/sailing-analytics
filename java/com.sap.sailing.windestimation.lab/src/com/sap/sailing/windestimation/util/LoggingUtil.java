package com.sap.sailing.windestimation.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingUtil {

    private static final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static AsynchronousFileChannel fileChannel = null;
    private static long filePos = 0;

    static {
        Path path = Paths.get("log" + logTimeFormatter.format(LocalDateTime.now()).replace(':', '_') + ".log");
        try {
            fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LoggingUtil() {

    }

    public static synchronized void logInfo(String logMessage) {
        String logOutput = logTimeFormatter.format(LocalDateTime.now()) + " " + logMessage + "\r\n";
        System.out.print(logOutput);
        byte[] bytes = logOutput.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        fileChannel.write(byteBuffer, filePos);
        filePos += bytes.length;
    }

}
