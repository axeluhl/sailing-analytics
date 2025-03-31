package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.io.Serializable;

public class RepeatablePartDTO implements Serializable {
    private static final long serialVersionUID = -687014482562838593L;

    private int zeroBasedIndexOfRepeatablePartStart;
    private int zeroBasedIndexOfRepeatablePartEnd;

    @Deprecated
    RepeatablePartDTO() {
        // GWT serialization
    }

    public RepeatablePartDTO(int zeroBasedIndexOfRepeatablePartStart, int zeroBasedIndexOfRepeatablePartEnd) {
        this.zeroBasedIndexOfRepeatablePartStart = zeroBasedIndexOfRepeatablePartStart;
        this.zeroBasedIndexOfRepeatablePartEnd = zeroBasedIndexOfRepeatablePartEnd;
    }

    public int getZeroBasedIndexOfRepeatablePartEnd() {
        return zeroBasedIndexOfRepeatablePartEnd;
    }

    public int getZeroBasedIndexOfRepeatablePartStart() {
        return zeroBasedIndexOfRepeatablePartStart;
    }

}
