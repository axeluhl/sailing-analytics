package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.BoatResultInRace;
import com.sap.sailing.kiworesultimport.RaceSummary;
import com.sap.sailing.kiworesultimport.RegattaSummary;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.Start;
import com.sap.sailing.kiworesultimport.StartReport;
import com.sap.sailing.kiworesultimport.ZipFile;
import com.sap.sse.common.Util;

public class ZipFileImpl implements ZipFile {
    private final Iterable<StartReport> startReports;
    private final Map<Util.Triple<String, Integer, String>, Start> startsByBoatclassAndRaceNumberAndFleetName;
    private final Iterable<ResultList> resultLists;
    private final Map<String, RegattaSummary> regattaSummaryByBoatClass;
    
    public ZipFileImpl(Iterable<StartReport> startReports, Iterable<ResultList> resultLists) {
        this.regattaSummaryByBoatClass = new HashMap<>();
        Map<String, ResultList> latestResultListForBoatClass = new HashMap<>();
        HashMap<String, Map<Integer, Set<String>>> fleetNamesPerBoatClassAndRaceNumber = new HashMap<String, Map<Integer, Set<String>>>();
        this.startReports = startReports;
        this.startsByBoatclassAndRaceNumberAndFleetName = new HashMap<Util.Triple<String, Integer, String>, Start>();
        this.resultLists = resultLists;
        for (StartReport startReport : startReports) {
            for (Start start : startReport.getStarts()) {
                startsByBoatclassAndRaceNumberAndFleetName.put(
                        new Util.Triple<String, Integer, String>(start.getBoatClass(), start.getRaceNumber(), start
                                .getFleetName()), start);
                Map<Integer, Set<String>> fleetsForBoatClass = fleetNamesPerBoatClassAndRaceNumber.get(start.getBoatClass());
                if (fleetsForBoatClass == null) {
                    fleetsForBoatClass = new HashMap<Integer, Set<String>>();
                    fleetNamesPerBoatClassAndRaceNumber.put(start.getBoatClass(), fleetsForBoatClass);
                }
                Set<String> fleetsForTheBoatClassAndRace = fleetsForBoatClass.get(start.getRaceNumber());
                if (fleetsForTheBoatClassAndRace == null) {
                    fleetsForTheBoatClassAndRace = new HashSet<String>();
                    fleetsForBoatClass.put(start.getRaceNumber(), fleetsForTheBoatClassAndRace);
                }
                fleetsForTheBoatClassAndRace.add(start.getFleetName());
            }
        }
        // use the latest result lists per boat class only to construct RegattaSummary objects
        for (ResultList resultList : resultLists) {
            ResultList latestSoFar = latestResultListForBoatClass.get(resultList.getBoatClassName());
            if (latestSoFar == null || latestSoFar.getTimePointPublished().compareTo(resultList.getTimePointPublished()) < 0) {
                latestResultListForBoatClass.put(resultList.getBoatClassName(), resultList);
            }
        }
        for (Map.Entry<String, ResultList> boatClassNameAndLatestResultList : latestResultListForBoatClass.entrySet()) {
            ResultList resultList = boatClassNameAndLatestResultList.getValue();
            List<RaceSummary> raceSummaries = new ArrayList<RaceSummary>();
            for (Integer raceNumber : resultList.getRaceNumbers()) {
                Map<Boat, BoatResultInRace> resultsPerBoat = new HashMap<>();
                for (Boat boat : resultList.getBoats()) {
                    resultsPerBoat.put(boat, boat.getResultsInRace(raceNumber));
                }
                raceSummaries.add(new RaceSummaryImpl(boatClassNameAndLatestResultList.getKey(), resultsPerBoat,
                        fleetNamesPerBoatClassAndRaceNumber.get(boatClassNameAndLatestResultList.getKey()).get(raceNumber), raceNumber));
            }
            RegattaSummary regattaSummary = new RegattaSummaryImpl(boatClassNameAndLatestResultList.getValue()
                    .getEvent(), raceSummaries, boatClassNameAndLatestResultList.getKey(), resultList.getBoats(),
                    resultList.getTimePointPublished());
            regattaSummaryByBoatClass.put(regattaSummary.getBoatClassName(), regattaSummary);
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
    
    @Override
    public RegattaSummary getRegattaSummary(String boatClassName) {
        return regattaSummaryByBoatClass.get(boatClassName);
    }

    @Override
    public Iterable<String> getBoatClassNames() {
        return regattaSummaryByBoatClass.keySet();
    }

    @Override
    public Iterable<RegattaSummary> getRegattaSummaries() {
        return regattaSummaryByBoatClass.values();
    }
    
}
