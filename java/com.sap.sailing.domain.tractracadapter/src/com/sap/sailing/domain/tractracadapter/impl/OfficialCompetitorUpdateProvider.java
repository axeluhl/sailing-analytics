package com.sap.sailing.domain.tractracadapter.impl;

public interface OfficialCompetitorUpdateProvider {
    /**
     * Runs the {@link Runnable} when all competitor update requests enqueued so far have been
     * processed.
     */
    void runWhenNoMoreOfficialCompetitorUpdatesPending(Runnable runnable);
}
