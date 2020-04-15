package com.sap.sailing.domain.base;

public interface MigratableRegatta extends Regatta {
    void migrateCanBoatsOfCompetitorsChangePerRace();
}
