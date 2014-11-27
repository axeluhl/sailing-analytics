package com.sap.sailing.domain.abstractlog.regattalog;



public interface RegattaLogEventVisitor {
    void visit(RegattaLogRevokeEvent event);
}
