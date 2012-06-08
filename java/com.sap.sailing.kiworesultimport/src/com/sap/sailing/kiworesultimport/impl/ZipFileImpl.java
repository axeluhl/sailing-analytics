package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.kiworesultimport.ResultList;
import com.sap.sailing.kiworesultimport.Start;
import com.sap.sailing.kiworesultimport.Startbericht;
import com.sap.sailing.kiworesultimport.ZipFile;

public class ZipFileImpl implements ZipFile {
    private final Iterable<Startbericht> startberichte;
    private final Map<Triple<String, Integer, String>, Start> startsByBoatclassAndWettfahrtAndFleet;
    private final List<ResultList> resultLists;
    private final Map<String, Map<Integer, Set<String>>> fleetsPerBoatClassAndRace;
    
    public ZipFileImpl(Iterable<Startbericht> startberichte, Iterable<ResultList> resultLists) {
        this.fleetsPerBoatClassAndRace = new HashMap<String, Map<Integer, Set<String>>>();
        this.startberichte = new ArrayList<Startbericht>();
        this.startsByBoatclassAndWettfahrtAndFleet = new HashMap<Triple<String, Integer, String>, Start>();
        this.resultLists = new ArrayList<ResultList>();
        for (Startbericht startbericht : startberichte) {
            for (Start start : startbericht.getStarts()) {
                startsByBoatclassAndWettfahrtAndFleet.put(
                        new Triple<String, Integer, String>(start.getBoatClass(), start.getRaceNumber(), start
                                .getFleetName()), start);
                Map<Integer, Set<String>> fleetsForBoatClass = fleetsPerBoatClassAndRace.get(start.getBoatClass());
                if (fleetsForBoatClass == null) {
                    fleetsForBoatClass = new HashMap<Integer, Set<String>>();
                    fleetsPerBoatClassAndRace.put(start.getBoatClass(), fleetsForBoatClass);
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
    public Iterable<Startbericht> getStartberichte() {
        return startberichte;
    }

    @Override
    public Iterable<ResultList> getResultLists() {
        return resultLists;
    }

}
