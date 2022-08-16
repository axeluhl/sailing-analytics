package com.sap.sailing.domain.coursetemplate;

public interface WithOptionalRepeatablePart {
    default boolean hasRepeatablePart() {
        return getRepeatablePart() != null;
    }
    
    RepeatablePart getRepeatablePart();
}
