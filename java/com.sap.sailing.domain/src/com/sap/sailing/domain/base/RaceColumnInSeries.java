package com.sap.sailing.domain.base;

public interface RaceColumnInSeries extends RaceColumn {
    Series getSeries();

    Regatta getRegatta();
}
