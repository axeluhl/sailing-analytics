package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/** Contains the result of a single statistic, to allow async loading of statistics without blocking the rest **/
public class SailorProfileStatisticDTO implements Result, Serializable {
    private static final long serialVersionUID = 2924378586764418626L;
    private Map<String, SingleEntry> result = new HashMap<>();

    // GWTSerialisation only
    protected SailorProfileStatisticDTO() {
        super();
    }

    public SailorProfileStatisticDTO(Map<String, SingleEntry> result) {
        this.result.putAll(result);
    }

    public Map<String, SingleEntry> getResult() {
        return result;
    }

    public static class SingleEntry implements Serializable {
        private static final long serialVersionUID = -7722750678632551505L;

        @GwtIncompatible
        public SingleEntry(Double value, RegattaAndRaceIdentifier relatedRaceOrNull,
                TimePoint relatedTimePointOrNull) {
            super();
            this.value = value;
            this.relatedRaceOrNull = relatedRaceOrNull;
            if (relatedTimePointOrNull != null) {
                // not all TimePoints are GWT compatible, ensure we have a compatible one!
                if (relatedTimePointOrNull instanceof MillisecondsTimePoint) {
                    this.relatedTimePointOrNull = (MillisecondsTimePoint) relatedTimePointOrNull;
                } else {
                    this.relatedTimePointOrNull = new MillisecondsTimePoint(relatedTimePointOrNull.asMillis());
                }
            }
        }

        // GWTSerialisation only
        protected SingleEntry() {
        }

        private Double value;
        private RegattaAndRaceIdentifier relatedRaceOrNull;
        // not generic, to reduce possible permutations for gwt compiler
        private MillisecondsTimePoint relatedTimePointOrNull;

        /**
         * All values will be in SI Units if not otherwise stated in the Type documentation
         */
        public Double getValue() {
            return value;
        }

        public RegattaAndRaceIdentifier getRelatedRaceOrNull() {
            return relatedRaceOrNull;
        }

        public MillisecondsTimePoint getRelatedTimePointOrNull() {
            return relatedTimePointOrNull;
        }

        @Override
        public String toString() {
            return "SingleEntry [" + (value != null ? "value=" + value + ", " : "")
                    + (relatedRaceOrNull != null ? "relatedRaceOrNull=" + relatedRaceOrNull + ", " : "")
                    + (relatedTimePointOrNull != null ? "relatedTimePointOrNull=" + relatedTimePointOrNull : "") + "]";
        }

    }
}
