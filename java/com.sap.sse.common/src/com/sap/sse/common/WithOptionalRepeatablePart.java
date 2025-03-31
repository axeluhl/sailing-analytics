package com.sap.sse.common;

public interface WithOptionalRepeatablePart {
    default boolean hasRepeatablePart() {
        return getRepeatablePart() != null;
    }
    
    RepeatablePart getRepeatablePart();
}
