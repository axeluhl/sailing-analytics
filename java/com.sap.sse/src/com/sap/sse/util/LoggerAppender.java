package com.sap.sse.util;

import java.io.IOException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Appends to a {@link Logger} with a given {@link Level}. Can, e.g., be used together with
 * {@link Formatter#Formatter(Appendable)} to obtain a formatter that writes its output into
 * the log.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LoggerAppender implements Appendable {
    private final Level level;
    private final Logger logger;

    public LoggerAppender(Level level, Logger logger) {
        super();
        this.level = level;
        this.logger = logger;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        logger.log(level, csq.toString());
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        logger.log(level, csq.subSequence(start, end).toString());
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        logger.log(level, ""+c);
        return this;
    }

}
