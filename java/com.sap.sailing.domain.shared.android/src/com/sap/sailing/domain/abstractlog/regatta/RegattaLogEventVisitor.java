package com.sap.sailing.domain.abstractlog.regatta;



public interface RegattaLogEventVisitor {
    void visit(RegattaLogRevokeEvent event);
}
