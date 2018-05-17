package com.sap.sailing.gwt.autoplay.client.nodes.base;

import java.util.function.Consumer;

public interface ProvidesDuration {

    void setDurationConsumer(Consumer<Integer> consumer);
    
}
