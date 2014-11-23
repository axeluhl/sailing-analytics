package com.sap.sse.util.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stream decorator that counts the bytes sent through an {@link OutputStream}. Logs
 * that number in configurable way using a {@link Logger}.
 * 
 * @author Axel Uhl
 */
public class CountingOutputStream extends FilterOutputStream {
    private static final Logger logger = Logger.getLogger(CountingOutputStream.class.getName());
    
    private long count;
    private final long logEveryHowManyBytes;
    private long lastMultiple;
    private final Level level;
    private final String name;
    
    public CountingOutputStream(OutputStream out, long logEveryHowManyBytes, Level logLevel, String name) {
        super(out);
        this.level = logLevel;
        this.name = name;
        this.logEveryHowManyBytes = logEveryHowManyBytes;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        count(1);
    }

    private void count(int bytesWritten) {
        count += bytesWritten;
        if (count >= (lastMultiple+1)*logEveryHowManyBytes) {
            lastMultiple++;
            log();
        }
    }

    private void log() {
        logger.log(level, "wrote "+count+" bytes "+(count>1000?"("+count/1000+" kB)":"")+" to "+name);
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        count(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        count(len);
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        log();
    }
}
