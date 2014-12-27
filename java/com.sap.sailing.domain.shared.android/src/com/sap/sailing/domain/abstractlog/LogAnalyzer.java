package com.sap.sailing.domain.abstractlog;

public interface LogAnalyzer<ResultType> {
    ResultType analyze();
}
