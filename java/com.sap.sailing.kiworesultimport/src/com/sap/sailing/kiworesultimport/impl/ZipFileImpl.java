package com.sap.sailing.kiworesultimport.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.Start;
import com.sap.sailing.kiworesultimport.StartReport;
import com.sap.sailing.kiworesultimport.ZipFile;

public class ZipFileImpl implements ZipFile {
    private final Iterable<StartReport> startReports;
    private final Map<Triple<String, Integer, String>, Start> startsByBoatclassAndRaceNumberAndFleetName;
    private final Iterable<ResultList> resultLists;
    private final Map<String, Map<Integer, Set<String>>> fleetsPerBoatClassAndRaceNumber;
    
    public ZipFileImpl(Iterable<StartReport> startReports, Iterable<ResultList> resultLists) {
        this.fleetsPerBoatClassAndRaceNumber = new HashMap<String, Map<Integer, Set<String>>>();
        this.startReports = startReports;
        this.startsByBoatclassAndRaceNumberAndFleetName = new HashMap<Triple<String, Integer, String>, Start>();
        this.resultLists = resultLists;
        for (StartReport startReport : startReports) {
            for (Start start : startReport.getStarts()) {
                startsByBoatclassAndRaceNumberAndFleetName.put(
                        new Triple<String, Integer, String>(start.getBoatClass(), start.getRaceNumber(), start
                                .getFleetName()), start);
                Map<Integer, Set<String>> fleetsForBoatClass = fleetsPerBoatClassAndRaceNumber.get(start.getBoatClass());
                if (fleetsForBoatClass == null) {
                    fleetsForBoatClass = new HashMap<Integer, Set<String>>();
                    fleetsPerBoatClassAndRaceNumber.put(start.getBoatClass(), fleetsForBoatClass);
                }
                Set<String> fleetsForTheBoatClassAndRace = fleetsForBoatClass.get(start.getRaceNumber());
                if (fleetsForTheBoatClassAndRace == null) {
                    fleetsForTheBoatClassAndRace = new HashSet<String>();
                    fleetsForBoatClass.put(start.getRaceNumber(), fleetsForTheBoatClassAndRace);
                }
                fleetsForTheBoatClassAndRace.add(start.getFleetName());
            }
        }
    }
    
    @Override
    public Iterable<StartReport> getStartReports() {
        return startReports;
    }

    @Override
    public Iterable<ResultList> getResultLists() {
        return resultLists;
    }

}
