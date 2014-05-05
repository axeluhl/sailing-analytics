package com.sap.sse.datamining.shared.components;

import com.sap.sse.datamining.shared.Message;

public enum AggregatorType {
    Sum, Average, Median;

    public Message getNameMessage() {
        switch (this) {
        case Average:
            return Message.Average;
        case Median:
            return Message.Median;
        case Sum:
            return Message.Sum;
        };
        throw new IllegalArgumentException("No message available for the aggregator type '" + this + "'");
    }
}