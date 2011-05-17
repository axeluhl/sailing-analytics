package com.sap.sailing.domain.tracking;



public interface DynamicTrackedLeg extends TrackedLeg {
    void completed(MarkPassing markPassing);
}
