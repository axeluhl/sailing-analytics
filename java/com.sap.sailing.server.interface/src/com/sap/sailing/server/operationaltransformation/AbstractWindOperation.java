package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;

public abstract class AbstractWindOperation extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 7565080467243441911L;
    private final WindSource windSource;
    private final Wind wind;
    
    public AbstractWindOperation(RegattaAndRaceIdentifier raceIdentifier, WindSource windSource, Wind wind) {
        super(raceIdentifier);
        this.windSource = windSource;
        this.wind = wind;
    }

    /**
     * Operations of this type can be run in parallel to other operations; subsequent operations do not have to wait
     * for this operation's completion.
     */
    @Override
    public boolean requiresSynchronousExecution() {
        return false;
    }

    protected WindSource getWindSource() {
        return windSource;
    }

    protected Wind getWind() {
        return wind;
    }
}
