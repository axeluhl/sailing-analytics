package com.sap.sailing.gwt.autoplay.client.nodes.base;

import java.util.function.Consumer;

public interface ProvidesDuration {
    /**
     * Will be called before onStart, if it does not send a time within ~30seconds, the node will be terminated.
     * A time can be supplid multiple times, the timer will always restart if newly called
     */
    void setDurationConsumer(Consumer<Integer> consumer);
    
}
