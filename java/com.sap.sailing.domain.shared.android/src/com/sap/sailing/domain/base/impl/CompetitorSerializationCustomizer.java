package com.sap.sailing.domain.base.impl;

import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;

public interface CompetitorSerializationCustomizer {
    
    boolean removalOfPersonalDataNecessary(Competitor competitor);
    
    public static final ThreadLocal<CompetitorSerializationCustomizer> threadLocalCustomizer = new ThreadLocal<>();
    
    public static final CompetitorSerializationCustomizer defaultCustomizer = new CompetitorSerializationCustomizer() {
        @Override
        public boolean removalOfPersonalDataNecessary(Competitor competitor) {
            return false;
        }
    };
    
    public static CompetitorSerializationCustomizer getCurrentCustomizer() {
        CompetitorSerializationCustomizer result = threadLocalCustomizer.get();
        if (result == null) {
            result = defaultCustomizer;
        }
        return result;
    }
    
    public static void doWithCustomizer(CompetitorSerializationCustomizer customizer, Runnable action) {
        threadLocalCustomizer.set(customizer);
        try {
            action.run();
        } finally {
            threadLocalCustomizer.remove();
        }
    }
    
    public static <T> T doWithCustomizer(CompetitorSerializationCustomizer customizer, Supplier<T> action) {
        threadLocalCustomizer.set(customizer);
        try {
            return action.get();
        } finally {
            threadLocalCustomizer.remove();
        }
    }

}
