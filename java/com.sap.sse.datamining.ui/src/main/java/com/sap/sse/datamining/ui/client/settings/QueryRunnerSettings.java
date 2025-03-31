package com.sap.sse.datamining.ui.client.settings;

import com.sap.sse.common.settings.AbstractSettings;

public class QueryRunnerSettings extends AbstractSettings {
    public static enum OtherChangedQueriesRunStrategy {
        /**
         * Other queries that changed implicitly will not be run automatically when running the
         * current query in the current result presenter, and the user will not even be asked.
         */
        NO,
        
        /**
         * When queries other than the current query from the current result presenter have implicitly
         * been modified and have not been run yet, the user will be asked whether to run them.
         */
        ASK,
        
        /**
         * When queries other than the current query from the current result presenter have implicitly
         * been modified and have not been run yet they will be run automatically in the background
         * as the user runs the current query from the current result presenter.
         */
        AUTOMATICALLY;
    }
    
    private boolean runAutomatically;
    
    private OtherChangedQueriesRunStrategy otherChangedQueriesRunStrategy;
    
    public QueryRunnerSettings() {
        this(false, OtherChangedQueriesRunStrategy.ASK);
    }

    public QueryRunnerSettings(boolean runAutomatically, OtherChangedQueriesRunStrategy otherChangedQueriesRunStrategy) {
        this.runAutomatically = runAutomatically;
        this.otherChangedQueriesRunStrategy = otherChangedQueriesRunStrategy;
    }

    public QueryRunnerSettings(QueryRunnerSettings settings) {
        this(settings.isRunAutomatically(), settings.getOtherChangedQueriesRunStrategy());
    }

    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    protected void setRunAutomatically(boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
    }
    
    public OtherChangedQueriesRunStrategy getOtherChangedQueriesRunStrategy() {
        return otherChangedQueriesRunStrategy;
    }
    
    protected void setOtherChangedQueriesRunStrategy(OtherChangedQueriesRunStrategy otherChangedQueriesRunStrategy) {
        this.otherChangedQueriesRunStrategy = otherChangedQueriesRunStrategy;
    }
    
}
