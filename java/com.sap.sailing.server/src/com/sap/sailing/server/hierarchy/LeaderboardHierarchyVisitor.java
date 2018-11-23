package com.sap.sailing.server.hierarchy;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface LeaderboardHierarchyVisitor {

    void visit(TrackedRace race);

    void visit(Competitor competitor);

    void visit(Boat boat);

}
