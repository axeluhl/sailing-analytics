package com.sap.sse.datamining.impl.data;

public enum ComparisonStrategy {
    
    LOWER_THAN("[") {
        @Override
        public boolean verifyComparisonResult(int comparisonResult) {
            return comparisonResult <= -1;
        }
    },
    
    LOWER_EQUALS_THAN("]") {
        @Override
        public boolean verifyComparisonResult(int comparisonResult) {
            return comparisonResult == 0 || comparisonResult <= -1;
        }
    },
    
    GREATER_THAN("]") {
        @Override
        public boolean verifyComparisonResult(int comparisonResult) {
            return comparisonResult >= 1;
        }
    },
    
    GREATER_EQUALS_THAN("[") {
        @Override
        public boolean verifyComparisonResult(int comparisonResult) {
            return comparisonResult == 0 || comparisonResult >= 1;
        }
    };
    
    private String signifier = null;
    
    private ComparisonStrategy(String signifier) {
        this.signifier = signifier;
    }

    public String getSignifier() {
        return signifier;
    }

    public abstract boolean verifyComparisonResult(int comparisonResult);
}