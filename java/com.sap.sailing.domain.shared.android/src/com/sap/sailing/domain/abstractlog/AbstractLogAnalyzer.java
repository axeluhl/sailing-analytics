package com.sap.sailing.domain.abstractlog;


/**
 * Analyzer to perform a query over a given AbstractLog. Each subclass defines its <code>ResultType</code>.
 * 
 * @param <ResultType>
 *            type of analysis result.
 */
public abstract class AbstractLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>,
EventT extends AbstractLogEvent<VisitorT>, VisitorT, ResultType> {

    protected LogT log;

    public AbstractLogAnalyzer(LogT log) {
        this.log = log;
    }

    public LogT getLog() {
        return log;
    }

    public ResultType analyze() {
        log.lockForRead();
        try {
            return performAnalysis();
        } finally {
            log.unlockAfterRead();
        }
    }

    protected abstract ResultType performAnalysis();

    protected Iterable<EventT> getAllEvents() {
        return log.getRawFixes();
    }

    protected Iterable<EventT> getAllEventsDescending() {
        return log.getRawFixesDescending();
    }
}
