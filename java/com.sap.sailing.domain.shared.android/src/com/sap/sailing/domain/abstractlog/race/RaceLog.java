package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sse.common.WithID;

/**
 * "Fix" validity is decided based on the {@link #getCurrentPassId() current pass}. The validity is not cached.
 */
public interface RaceLog extends AbstractLog<RaceLogEvent, RaceLogEventVisitor>, WithID {
    public static final int DefaultPassId = 0;

    class PassValidator implements AbstractLogImpl.NavigableSetViewValidator<RaceLogEvent> {
        final int pass;

        public PassValidator(int pass) {
            this.pass = pass;
        }

        @Override
        public boolean isValid(RaceLogEvent item) {
            return item.getPassId() == pass;
        }
    }

    /**
     * Gets the current pass id.
     *
     * @return the pass id.
     */
    int getCurrentPassId();

}
